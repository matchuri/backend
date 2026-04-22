package matchuri.backend.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.auth.support.token.RefreshTokenCookieService;
import matchuri.backend.domain.member.entity.SocialProviderType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final MatchuriOAuth2AuthorizationRequestRepository authorizationRequestRepository;
    private final RefreshTokenCookieService refreshTokenCookieService;
    private final OAuth2RedirectService redirectService;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        authorizationRequestRepository.clearAuthorizationRequest(request, response);
        refreshTokenCookieService.clearRefreshToken(response);

        SocialProviderType provider = resolveProvider(request);
        AuthErrorCode errorCode = resolveErrorCode(exception);
        log.warn("auth event=oauth2_provider_failed provider={} ip={} code={}", provider.toRegistrationId(), request.getRemoteAddr(), errorCode.getCode());

        response.sendRedirect(redirectService.buildFailureRedirectUrl(provider, errorCode));
    }

    private AuthErrorCode resolveErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2AuthenticationException
                && "access_denied".equals(oauth2AuthenticationException.getError().getErrorCode())) {
            return AuthErrorCode.OAUTH2_PROVIDER_REJECTED;
        }

        return AuthErrorCode.OAUTH2_PROCESSING_FAILED;
    }

    private SocialProviderType resolveProvider(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri == null || requestUri.isBlank()) {
            return SocialProviderType.GOOGLE;
        }

        String[] segments = requestUri.split("/");
        return SocialProviderType.fromRegistrationId(segments[segments.length - 1]);
    }
}
