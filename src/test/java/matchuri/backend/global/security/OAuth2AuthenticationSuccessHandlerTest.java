package matchuri.backend.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.auth.result.OAuth2LoginResult;
import matchuri.backend.domain.auth.service.OAuth2LoginService;
import matchuri.backend.domain.auth.support.token.RefreshTokenCookieService;
import matchuri.backend.domain.member.exception.MemberErrorCode;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private OAuth2LoginService oAuth2LoginService;

    @Mock
    private RefreshTokenCookieService refreshTokenCookieService;

    @Mock
    private MatchuriOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Mock
    private OAuth2RedirectService redirectService;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    @Test
    @DisplayName("OAuth2 로그인 성공 시 쿠키를 정리하고 refresh token을 설정한 뒤 성공 URL로 리다이렉트한다")
    void redirectsToSuccessUrlAfterSettingCookies() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = authentication(createOAuth2User("google-user-1", "google@example.com", "구글사용자"));

        when(oAuth2LoginService.login(org.mockito.Mockito.eq(SocialProviderType.GOOGLE), any(OAuth2User.class), org.mockito.Mockito.eq("127.0.0.1")))
                .thenReturn(new OAuth2LoginResult(1L, "refresh-token", "exchange-code"));
        when(redirectService.buildSuccessRedirectUrl(SocialProviderType.GOOGLE, "exchange-code"))
                .thenReturn("http://localhost:3000/auth/callback/google?loginResult=success&provider=google&code=exchange-code");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(authorizationRequestRepository).clearAuthorizationRequest(request, response);
        verify(refreshTokenCookieService).addRefreshToken(response, "refresh-token");
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/auth/callback/google?loginResult=success&provider=google&code=exchange-code");
    }

    @Test
    @DisplayName("OAuth2 로그인 후속 처리 실패 시 refresh token 쿠키를 지우고 실패 URL로 리다이렉트한다")
    void redirectsToFailureUrlWhenPostLoginProcessingFails() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = authentication(createOAuth2User("google-user-1", "google@example.com", "구글사용자"));

        when(oAuth2LoginService.login(org.mockito.Mockito.eq(SocialProviderType.GOOGLE), any(OAuth2User.class), org.mockito.Mockito.eq("127.0.0.1")))
                .thenThrow(new IllegalStateException("boom"));
        when(redirectService.buildFailureRedirectUrl(SocialProviderType.GOOGLE, AuthErrorCode.OAUTH2_PROCESSING_FAILED))
                .thenReturn("http://localhost:3000/login?loginResult=failed&provider=google&errorCode=AUTH_OAUTH2_PROCESSING_FAILED");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(authorizationRequestRepository).clearAuthorizationRequest(request, response);
        verify(refreshTokenCookieService).clearRefreshToken(response);
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/login?loginResult=failed&provider=google&errorCode=AUTH_OAUTH2_PROCESSING_FAILED");
    }

    @Test
    @DisplayName("OAuth2 로그인 후속 처리에서 Matchuri 예외가 발생하면 원래 에러 코드를 유지한다")
    void preservesMatchuriErrorCodeWhenPostLoginProcessingFails() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = authentication(createOAuth2User("google-user-1", "google@example.com", "구글사용자"));

        when(oAuth2LoginService.login(org.mockito.Mockito.eq(SocialProviderType.GOOGLE), any(OAuth2User.class), org.mockito.Mockito.eq("127.0.0.1")))
                .thenThrow(new BusinessException(MemberErrorCode.INACTIVE_MEMBER));
        when(redirectService.buildFailureRedirectUrl(SocialProviderType.GOOGLE, MemberErrorCode.INACTIVE_MEMBER))
                .thenReturn("http://localhost:3000/login?loginResult=failed&provider=google&errorCode=MEMBER_INACTIVE_MEMBER");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(authorizationRequestRepository).clearAuthorizationRequest(request, response);
        verify(refreshTokenCookieService).clearRefreshToken(response);
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/login?loginResult=failed&provider=google&errorCode=MEMBER_INACTIVE_MEMBER");
    }

    private Authentication authentication(OAuth2User principal) {
        return new OAuth2AuthenticationToken(principal, java.util.List.of(), "google");
    }

    private OAuth2User createOAuth2User(String sub, String email, String name) {
        return new DefaultOAuth2User(
                java.util.List.of(),
                Map.of(
                        "sub", sub,
                        "email", email,
                        "name", name
                ),
                "sub"
        );
    }
}
