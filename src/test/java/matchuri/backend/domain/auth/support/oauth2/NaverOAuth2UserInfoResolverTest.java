package matchuri.backend.domain.auth.support.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import matchuri.backend.domain.member.entity.SocialProviderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

class NaverOAuth2UserInfoResolverTest {

    private final NaverOAuth2UserInfoResolver resolver = new NaverOAuth2UserInfoResolver();

    @Test
    @DisplayName("Naver OAuth2 사용자 정보를 공통 구조로 정규화한다")
    void resolvesNaverUserInfo() {
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "response", Map.of(
                                "id", "naver-user-1",
                                "email", "naver@example.com",
                                "nickname", "네이버사용자"
                        )
                ),
                "response"
        );

        OAuth2ProviderUserInfo userInfo = resolver.resolve(oauth2User);

        assertThat(resolver.supports(SocialProviderType.NAVER)).isTrue();
        assertThat(userInfo.provider()).isEqualTo(SocialProviderType.NAVER);
        assertThat(userInfo.providerUserId()).isEqualTo("naver-user-1");
        assertThat(userInfo.email()).isEqualTo("naver@example.com");
        assertThat(userInfo.nickname()).isEqualTo("네이버사용자");
    }

    @Test
    @DisplayName("Naver 계정 이메일과 닉네임이 없어도 고유 식별자를 정규화한다")
    void resolvesNaverUserInfoWithoutOptionalFields() {
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(),
                Map.of("response", Map.of("id", "naver-user-2")),
                "response"
        );

        OAuth2ProviderUserInfo userInfo = resolver.resolve(oauth2User);

        assertThat(userInfo.providerUserId()).isEqualTo("naver-user-2");
        assertThat(userInfo.email()).isNull();
        assertThat(userInfo.nickname()).isNull();
    }
}
