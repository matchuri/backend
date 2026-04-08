package matchuri.backend.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import matchuri.backend.domain.auth.AuthErrorCode;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.global.config.MatchuriProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OAuth2RedirectServiceTest {

    @Test
    @DisplayName("OAuth2 성공 리다이렉트 URL을 일관되게 생성한다")
    void buildsSuccessRedirectUrl() {
        OAuth2RedirectService service = new OAuth2RedirectService(createProperties());

        String redirectUrl = service.buildSuccessRedirectUrl(SocialProviderType.GOOGLE, "exchange-code");

        assertThat(redirectUrl).isEqualTo(
                "http://localhost:3000/auth/callback/google?loginResult=success&provider=google&code=exchange-code"
        );
    }

    @Test
    @DisplayName("OAuth2 실패 리다이렉트 URL을 일관되게 생성한다")
    void buildsFailureRedirectUrl() {
        OAuth2RedirectService service = new OAuth2RedirectService(createProperties());

        String redirectUrl = service.buildFailureRedirectUrl(SocialProviderType.NAVER, AuthErrorCode.OAUTH2_PROVIDER_REJECTED);

        assertThat(redirectUrl).isEqualTo(
                "http://localhost:3000/login?loginResult=failed&provider=naver&errorCode=AUTH_OAUTH2_PROVIDER_REJECTED"
        );
    }

    private MatchuriProperties createProperties() {
        MatchuriProperties properties = new MatchuriProperties();
        MatchuriProperties.Auth auth = new MatchuriProperties.Auth();
        MatchuriProperties.OAuth2 oauth2 = new MatchuriProperties.OAuth2();
        oauth2.setFrontendBaseUrl("http://localhost:3000");
        oauth2.setSuccessPath("/auth/callback/google");
        oauth2.setFailurePath("/login");
        oauth2.setExchangeCodeExpirationSeconds(300);
        auth.setOauth2(oauth2);
        properties.setAuth(auth);
        return properties;
    }
}
