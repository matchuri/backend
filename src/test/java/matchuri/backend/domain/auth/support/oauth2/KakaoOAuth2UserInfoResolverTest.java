package matchuri.backend.domain.auth.support.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import matchuri.backend.domain.member.entity.SocialProviderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

class KakaoOAuth2UserInfoResolverTest {

    private final KakaoOAuth2UserInfoResolver resolver = new KakaoOAuth2UserInfoResolver();

    @Test
    @DisplayName("Kakao OAuth2 사용자 정보를 공통 구조로 정규화한다")
    void resolvesKakaoUserInfo() {
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "id", 123456789L,
                        "kakao_account", Map.of("email", "kakao@example.com")
                ),
                "id"
        );

        OAuth2ProviderUserInfo userInfo = resolver.resolve(oauth2User);

        assertThat(resolver.supports(SocialProviderType.KAKAO)).isTrue();
        assertThat(userInfo.provider()).isEqualTo(SocialProviderType.KAKAO);
        assertThat(userInfo.providerUserId()).isEqualTo("123456789");
        assertThat(userInfo.email()).isEqualTo("kakao@example.com");
    }

    @Test
    @DisplayName("Kakao 계정 이메일이 없어도 고유 식별자를 정규화한다")
    void resolvesKakaoUserInfoWithoutOptionalFields() {
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "id", 987654321L,
                        "kakao_account", Map.of()
                ),
                "id"
        );

        OAuth2ProviderUserInfo userInfo = resolver.resolve(oauth2User);

        assertThat(userInfo.providerUserId()).isEqualTo("987654321");
        assertThat(userInfo.email()).isNull();
    }
}
