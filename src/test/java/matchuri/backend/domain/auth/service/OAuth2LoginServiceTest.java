package matchuri.backend.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.SocialProviderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginServiceTest {

    @Mock
    private OAuth2MemberService oAuth2MemberService;

    @Mock
    private SessionTokenService sessionTokenService;

    @Test
    @DisplayName("provider에 맞는 resolver로 사용자 정보를 정규화한 뒤 로그인 흐름을 진행한다")
    void loginResolvesProviderUserInfoBeforeIssuingTokens() {
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(),
                java.util.Map.of(
                        "sub", "google-user-1",
                        "email", "google@example.com",
                        "name", "구글사용자"
                ),
                "sub"
        );
        GoogleOAuth2UserInfoResolver resolver = new GoogleOAuth2UserInfoResolver();
        OAuth2LoginService service = new OAuth2LoginService(
                oAuth2MemberService,
                sessionTokenService,
                List.of(resolver)
        );
        Member member = Member.createSocialMember(SocialProviderType.GOOGLE, "google-user-1", "google@example.com", "구글사용자");
        TokenPair tokenPair = new TokenPair(
                "access-token",
                3600L,
                "refresh-token",
                LocalDateTime.of(2026, 4, 9, 12, 0)
        );

        when(oAuth2MemberService.findOrCreateMember(SocialProviderType.GOOGLE, "google-user-1", "google@example.com"))
                .thenReturn(member);
        when(sessionTokenService.issueLoginTokenPair(member)).thenReturn(tokenPair);
        when(sessionTokenService.createExchangeCode(member, SocialProviderType.GOOGLE)).thenReturn("exchange-code");

        OAuth2LoginResult result = service.login(SocialProviderType.GOOGLE, oauth2User, "127.0.0.1");

        verify(oAuth2MemberService).findOrCreateMember(SocialProviderType.GOOGLE, "google-user-1", "google@example.com");
        verify(sessionTokenService).issueLoginTokenPair(member);
        verify(sessionTokenService).createExchangeCode(member, SocialProviderType.GOOGLE);
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.exchangeCode()).isEqualTo("exchange-code");
    }
}
