package matchuri.backend.global.security;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.AuthErrorCode;
import matchuri.backend.global.config.MatchuriProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class GoogleOAuth2RedirectService {

    private final MatchuriProperties matchuriProperties;

    public String buildSuccessRedirectUrl(String exchangeCode) {
        MatchuriProperties.OAuth2 oauth2 = matchuriProperties.getAuth().getOauth2();
        return UriComponentsBuilder.fromUriString(oauth2.getFrontendBaseUrl())
                .path(oauth2.getSuccessPath())
                .queryParam("loginResult", "success")
                .queryParam("code", exchangeCode)
                .build()
                .toUriString();
    }

    public String buildFailureRedirectUrl(AuthErrorCode errorCode) {
        MatchuriProperties.OAuth2 oauth2 = matchuriProperties.getAuth().getOauth2();
        return UriComponentsBuilder.fromUriString(oauth2.getFrontendBaseUrl())
                .path(oauth2.getFailurePath())
                .queryParam("loginResult", "failed")
                .queryParam("provider", "google")
                .queryParam("errorCode", errorCode.getCode())
                .build()
                .toUriString();
    }
}
