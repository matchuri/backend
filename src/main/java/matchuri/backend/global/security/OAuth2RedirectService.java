package matchuri.backend.global.security;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.global.config.MatchuriProperties;
import matchuri.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2RedirectService {

    private final MatchuriProperties matchuriProperties;

    public String buildSuccessRedirectUrl(SocialProviderType provider, String exchangeCode) {
        MatchuriProperties.OAuth2 oauth2 = matchuriProperties.getAuth().getOauth2();
        return UriComponentsBuilder.fromUriString(oauth2.getFrontendBaseUrl())
                .path(oauth2.getSuccessPath())
                .pathSegment(provider.toRegistrationId())
                .queryParam("loginResult", "success")
                .queryParam("provider", provider.toRegistrationId())
                .queryParam("code", exchangeCode)
                .build()
                .toUriString();
    }

    public String buildFailureRedirectUrl(SocialProviderType provider, ErrorCode errorCode) {
        MatchuriProperties.OAuth2 oauth2 = matchuriProperties.getAuth().getOauth2();
        return UriComponentsBuilder.fromUriString(oauth2.getFrontendBaseUrl())
                .path(oauth2.getFailurePath())
                .queryParam("loginResult", "failed")
                .queryParam("provider", provider.toRegistrationId())
                .queryParam("errorCode", errorCode.getCode())
                .build()
                .toUriString();
    }
}
