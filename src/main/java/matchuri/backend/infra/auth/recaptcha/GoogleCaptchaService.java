package matchuri.backend.infra.auth.recaptcha;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.auth.service.CaptchaService;
import matchuri.backend.global.config.ReCaptchaConfig;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class GoogleCaptchaService implements CaptchaService {

    private final ReCaptchaConfig config;
    private final RestClient restClient;

    public GoogleCaptchaService(
            ReCaptchaConfig config,
            @Qualifier("recaptchaRestClient") RestClient restClient
    ) {
        this.config = config;
        this.restClient = restClient;
    }

    @Override
    public boolean verifyToken(String token, String expectedAction, String clientIp) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", config.getSecretKey());
        form.add("response", token);
        if (StringUtils.hasText(clientIp)) {
            form.add("remoteip", clientIp);
        }

        try {
            ReCaptchaVerificationResponse response = restClient.post()
                    .uri(config.getVerifyUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(ReCaptchaVerificationResponse.class);

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

            return expectedAction.equals(response.action())
                    && response.score() != null
                    && response.score() >= config.getScoreThreshold();
        } catch (RestClientException exception) {
            log.warn("auth event=captcha_provider_unavailable provider=google", exception);
            throw new BusinessException(AuthErrorCode.CAPTCHA_SERVICE_UNAVAILABLE);
        }
    }
}
