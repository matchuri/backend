package matchuri.backend.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import matchuri.backend.domain.auth.AuthErrorCode;
import matchuri.backend.domain.auth.service.RefreshTokenCookieService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

@ExtendWith(MockitoExtension.class)
class GoogleOAuth2AuthenticationFailureHandlerTest {

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Mock
    private RefreshTokenCookieService refreshTokenCookieService;

    @Mock
    private GoogleOAuth2RedirectService redirectService;

    @InjectMocks
    private GoogleOAuth2AuthenticationFailureHandler failureHandler;

    @Test
    @DisplayName("제공자 인증 거절은 AUTH_OAUTH2_PROVIDER_REJECTED로 리다이렉트한다")
    void redirectsWithProviderRejectedCode() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = new OAuth2AuthenticationException(new OAuth2Error("access_denied"));

        when(redirectService.buildFailureRedirectUrl(AuthErrorCode.OAUTH2_PROVIDER_REJECTED))
                .thenReturn("http://localhost:3000/login?loginResult=failed&provider=google&errorCode=AUTH_OAUTH2_PROVIDER_REJECTED");

        failureHandler.onAuthenticationFailure(request, response, exception);

        verify(authorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
        verify(refreshTokenCookieService).clearRefreshToken(response);
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/login?loginResult=failed&provider=google&errorCode=AUTH_OAUTH2_PROVIDER_REJECTED");
    }

    @Test
    @DisplayName("기타 OAuth2 실패는 AUTH_OAUTH2_PROCESSING_FAILED로 리다이렉트한다")
    void redirectsWithProcessingFailedCode() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = new OAuth2AuthenticationException(new OAuth2Error("server_error"));

        when(redirectService.buildFailureRedirectUrl(AuthErrorCode.OAUTH2_PROCESSING_FAILED))
                .thenReturn("http://localhost:3000/login?loginResult=failed&provider=google&errorCode=AUTH_OAUTH2_PROCESSING_FAILED");

        failureHandler.onAuthenticationFailure(request, response, exception);

        verify(authorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
        verify(refreshTokenCookieService).clearRefreshToken(response);
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/login?loginResult=failed&provider=google&errorCode=AUTH_OAUTH2_PROCESSING_FAILED");
    }
}
