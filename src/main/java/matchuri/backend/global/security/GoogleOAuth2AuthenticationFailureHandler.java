package matchuri.backend.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.AuthErrorCode;
import matchuri.backend.domain.auth.service.RefreshTokenCookieService;
import matchuri.backend.global.config.MatchuriProperties;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;
    private final RefreshTokenCookieService refreshTokenCookieService;
    private final MatchuriProperties matchuriProperties;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
        refreshTokenCookieService.clearRefreshToken(response);

        AuthErrorCode errorCode = resolveErrorCode(exception);
        log.warn("auth event=oauth2_provider_failed provider=google ip={} code={}", request.getRemoteAddr(), errorCode.getCode());

        response.sendRedirect(
                UriComponentsBuilder.fromUriString(matchuriProperties.getAuth().getOauth2().getFrontendBaseUrl())
                        .path(matchuriProperties.getAuth().getOauth2().getFailurePath())
                        .queryParam("loginResult", "failed")
                        .queryParam("provider", "google")
                        .queryParam("errorCode", errorCode.getCode())
                        .build()
                        .toUriString()
        );
    }

    private AuthErrorCode resolveErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2AuthenticationException
                && "access_denied".equals(oauth2AuthenticationException.getError().getErrorCode())) {
            return AuthErrorCode.OAUTH2_PROVIDER_REJECTED;
        }

        return AuthErrorCode.OAUTH2_PROCESSING_FAILED;
    }
}
