package matchuri.backend.infra.auth.captcha.google;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.auth.service.CaptchaPurpose;
import matchuri.backend.domain.auth.service.CaptchaVerifier;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@Profile("prod")
@ConditionalOnProperty(prefix = "captcha", name = "provider", havingValue = "google", matchIfMissing = true)
public class GoogleRecaptchaVerifier implements CaptchaVerifier {

    private final GoogleRecaptchaProperties properties;
    private final RestClient restClient;

    public GoogleRecaptchaVerifier(
            GoogleRecaptchaProperties properties,
            @Qualifier("googleCaptchaRestClient") RestClient restClient
    ) {
        this.properties = properties;
        this.restClient = restClient;
    }

    @Override
    public boolean verify(String token, CaptchaPurpose purpose, String clientIp) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", properties.getSecretKey());
        form.add("response", token);
        if (StringUtils.hasText(clientIp)) {
            form.add("remoteip", clientIp);
        }

        try {
            GoogleRecaptchaVerificationResponse response = restClient.post()
                    .uri(properties.getVerifyUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(GoogleRecaptchaVerificationResponse.class);

            if (response == null) {
                throw new BusinessException(AuthErrorCode.CAPTCHA_SERVICE_UNAVAILABLE);
            }
            if (response.hasSecretConfigurationError()) {
                log.error("auth event=captcha_provider_configuration_error provider=google errorCodes={}",
                        response.errorCodes());
                throw new BusinessException(AuthErrorCode.CAPTCHA_SERVICE_UNAVAILABLE);
            }
            if (!response.success()) {
                log.info("auth event=captcha_rejected provider=google errorCodes={}",
                        response.errorCodes() == null ? List.of() : response.errorCodes());
                return false;
            }

            return actionFor(purpose).equals(response.action())
                    && response.score() != null
                    && response.score() >= properties.getScoreThreshold();
        } catch (RestClientException exception) {
            log.warn("auth event=captcha_provider_unavailable provider=google", exception);
            throw new BusinessException(AuthErrorCode.CAPTCHA_SERVICE_UNAVAILABLE);
        }
    }

    private String actionFor(CaptchaPurpose purpose) {
        return switch (purpose) {
            case LOGIN -> "login";
        };
    }
}
