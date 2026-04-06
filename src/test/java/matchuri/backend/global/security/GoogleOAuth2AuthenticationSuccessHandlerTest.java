package matchuri.backend.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import matchuri.backend.domain.auth.AuthErrorCode;
import matchuri.backend.domain.auth.service.GoogleOAuth2LoginResult;
import matchuri.backend.domain.auth.service.GoogleOAuth2LoginService;
import matchuri.backend.domain.auth.service.RefreshTokenCookieService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
class GoogleOAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private GoogleOAuth2LoginService googleOAuth2LoginService;

    @Mock
    private RefreshTokenCookieService refreshTokenCookieService;

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Mock
    private GoogleOAuth2RedirectService redirectService;

    @InjectMocks
    private GoogleOAuth2AuthenticationSuccessHandler successHandler;

    @Test
    @DisplayName("OAuth2 로그인 성공 시 쿠키를 정리하고 refresh token을 설정한 뒤 성공 URL로 리다이렉트한다")
    void redirectsToSuccessUrlAfterSettingCookies() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = authentication(createOAuth2User("google-user-1", "google@example.com", "구글사용자"));

        when(googleOAuth2LoginService.login("google-user-1", "google@example.com", "구글사용자", "127.0.0.1"))
                .thenReturn(new GoogleOAuth2LoginResult(1L, "refresh-token", "exchange-code"));
        when(redirectService.buildSuccessRedirectUrl("exchange-code"))
                .thenReturn("http://localhost:3000/auth/callback/google?loginResult=success&code=exchange-code");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(authorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
        verify(refreshTokenCookieService).addRefreshToken(response, "refresh-token");
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/auth/callback/google?loginResult=success&code=exchange-code");
    }

    @Test
    @DisplayName("OAuth2 로그인 후속 처리 실패 시 refresh token 쿠키를 지우고 실패 URL로 리다이렉트한다")
    void redirectsToFailureUrlWhenPostLoginProcessingFails() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = authentication(createOAuth2User("google-user-1", "google@example.com", "구글사용자"));

        when(googleOAuth2LoginService.login("google-user-1", "google@example.com", "구글사용자", "127.0.0.1"))
                .thenThrow(new IllegalStateException("boom"));
        when(redirectService.buildFailureRedirectUrl(AuthErrorCode.OAUTH2_PROCESSING_FAILED))
                .thenReturn("http://localhost:3000/login?loginResult=failed&provider=google&errorCode=AUTH_OAUTH2_PROCESSING_FAILED");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(authorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
        verify(refreshTokenCookieService).clearRefreshToken(response);
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/login?loginResult=failed&provider=google&errorCode=AUTH_OAUTH2_PROCESSING_FAILED");
    }

    private Authentication authentication(OAuth2User principal) {
        return new TestingAuthenticationToken(principal, null);
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
