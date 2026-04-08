package matchuri.backend.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import matchuri.backend.domain.member.entity.SocialProviderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

class GoogleOAuth2UserInfoResolverTest {

    private final GoogleOAuth2UserInfoResolver resolver = new GoogleOAuth2UserInfoResolver();

    @Test
    @DisplayName("Google OAuth2 사용자 정보를 공통 구조로 정규화한다")
    void resolvesGoogleUserInfo() {
        OAuth2User oauth2User = new DefaultOAuth2User(
                java.util.List.of(),
                Map.of(
                        "sub", "google-user-1",
                        "email", "google@example.com",
                        "name", "구글사용자"
                ),
                "sub"
        );

        OAuth2ProviderUserInfo userInfo = resolver.resolve(oauth2User);

        assertThat(resolver.supports(SocialProviderType.GOOGLE)).isTrue();
        assertThat(userInfo.provider()).isEqualTo(SocialProviderType.GOOGLE);
        assertThat(userInfo.providerUserId()).isEqualTo("google-user-1");
        assertThat(userInfo.email()).isEqualTo("google@example.com");
        assertThat(userInfo.nickname()).isEqualTo("구글사용자");
    }
}
