package matchuri.backend.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.auth.result.OAuth2LoginResult;
import matchuri.backend.domain.auth.service.OAuth2LoginService;
import matchuri.backend.domain.auth.support.token.RefreshTokenCookieService;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.global.exception.ErrorCode;
import matchuri.backend.global.exception.MatchuriException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2LoginService oAuth2LoginService;
    private final RefreshTokenCookieService refreshTokenCookieService;
    private final MatchuriOAuth2AuthorizationRequestRepository authorizationRequestRepository;
    private final OAuth2RedirectService redirectService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        SocialProviderType provider = resolveProvider(authentication);
        try {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            OAuth2LoginResult loginResult = oAuth2LoginService.login(
                    provider,
                    oauth2User,
                    request.getRemoteAddr()
            );

            authorizationRequestRepository.clearAuthorizationRequest(request, response);
            refreshTokenCookieService.addRefreshToken(response, loginResult.refreshToken());

            String redirectUrl = redirectService.buildSuccessRedirectUrl(provider, loginResult.exchangeCode());
            response.sendRedirect(redirectUrl);

        } catch (Exception exception) {
            ErrorCode errorCode = resolveErrorCode(exception);
            log.warn(
                    "auth event=oauth2_login_failed provider={} ip={} code={} reason={}",
                    provider.toRegistrationId(),
                    request.getRemoteAddr(),
                    errorCode.getCode(),
                    exception.getMessage()
            );
            authorizationRequestRepository.clearAuthorizationRequest(request, response);
            refreshTokenCookieService.clearRefreshToken(response);

            String redirectUrl = redirectService.buildFailureRedirectUrl(provider, errorCode);
            response.sendRedirect(redirectUrl);

        }
    }

    private ErrorCode resolveErrorCode(Exception exception) {
        if (exception instanceof MatchuriException matchuriException) {
            return matchuriException.getErrorCode();
        }

        return AuthErrorCode.OAUTH2_PROCESSING_FAILED;
    }

    private SocialProviderType resolveProvider(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken oauth2AuthenticationToken) {
            return SocialProviderType.fromRegistrationId(oauth2AuthenticationToken.getAuthorizedClientRegistrationId());
        }

        throw new matchuri.backend.global.exception.AuthenticationException(AuthErrorCode.OAUTH2_PROVIDER_NOT_SUPPORTED);
    }
}
