package matchuri.backend.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.AuthErrorCode;
import matchuri.backend.domain.auth.service.GoogleOAuth2LoginResult;
import matchuri.backend.domain.auth.service.GoogleOAuth2LoginService;
import matchuri.backend.domain.auth.service.RefreshTokenCookieService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final GoogleOAuth2LoginService googleOAuth2LoginService;
    private final RefreshTokenCookieService refreshTokenCookieService;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;
    private final GoogleOAuth2RedirectService redirectService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        try {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            GoogleOAuth2LoginResult loginResult = googleOAuth2LoginService.login(
                    oauth2User.getAttribute("sub"),
                    oauth2User.getAttribute("email"),
                    oauth2User.getAttribute("name"),
                    request.getRemoteAddr()
            );

            authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
            refreshTokenCookieService.addRefreshToken(response, loginResult.refreshToken());

            response.sendRedirect(redirectService.buildSuccessRedirectUrl(loginResult.exchangeCode()));
        } catch (Exception exception) {
            log.warn("auth event=oauth2_login_failed provider=google ip={} reason={}", request.getRemoteAddr(), exception.getMessage());
            authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
            refreshTokenCookieService.clearRefreshToken(response);
            response.sendRedirect(redirectService.buildFailureRedirectUrl(AuthErrorCode.OAUTH2_PROCESSING_FAILED));
        }
    }
}
