package matchuri.backend.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.AuthErrorCode;
import matchuri.backend.domain.auth.service.GoogleOAuth2Service;
import matchuri.backend.domain.auth.service.RefreshTokenCookieService;
import matchuri.backend.domain.auth.service.SessionTokenService;
import matchuri.backend.domain.auth.service.TokenPair;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.global.config.MatchuriProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final GoogleOAuth2Service googleOAuth2Service;
    private final SessionTokenService sessionTokenService;
    private final RefreshTokenCookieService refreshTokenCookieService;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;
    private final MatchuriProperties matchuriProperties;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        try {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String providerUserId = oauth2User.getAttribute("sub");
            String email = oauth2User.getAttribute("email");
            String nickname = oauth2User.getAttribute("name");

            Member member = googleOAuth2Service.findOrCreateMember(providerUserId, email, nickname);
            TokenPair tokenPair = sessionTokenService.issueLoginTokenPair(member);
            refreshTokenCookieService.addRefreshToken(response, tokenPair.refreshToken());
            String exchangeCode = sessionTokenService.createExchangeCode(member, SocialProviderType.GOOGLE);

            authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
            log.info("auth event=oauth2_login_success provider=google memberId={} ip={}", member.getId(), request.getRemoteAddr());

            response.sendRedirect(buildSuccessRedirectUrl(exchangeCode));
        } catch (Exception exception) {
            log.warn("auth event=oauth2_login_failed provider=google ip={} reason={}", request.getRemoteAddr(), exception.getMessage());
            authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
            refreshTokenCookieService.clearRefreshToken(response);
            response.sendRedirect(buildFailureRedirectUrl(AuthErrorCode.OAUTH2_PROCESSING_FAILED));
        }
    }

    private String buildSuccessRedirectUrl(String exchangeCode) {
        MatchuriProperties.OAuth2 oauth2 = matchuriProperties.getAuth().getOauth2();
        return UriComponentsBuilder.fromUriString(oauth2.getFrontendBaseUrl())
                .path(oauth2.getSuccessPath())
                .queryParam("loginResult", "success")
                .queryParam("code", exchangeCode)
                .build()
                .toUriString();
    }

    private String buildFailureRedirectUrl(AuthErrorCode errorCode) {
        MatchuriProperties.OAuth2 oauth2 = matchuriProperties.getAuth().getOauth2();
        return UriComponentsBuilder.fromUriString(oauth2.getFrontendBaseUrl())
                .path(oauth2.getFailurePath())
                .queryParam("loginResult", "failed")
                .queryParam("provider", "google")
                .queryParam("errorCode", errorCode.getCode())
                .build()
                .toUriString();
    }
}
