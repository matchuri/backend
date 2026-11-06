package matchuri.backend.domain.auth.support.token;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.config.MatchuriProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieService {

    private final MatchuriProperties matchuriProperties;

    public void addRefreshToken(HttpServletResponse response, String refreshToken) {
        MatchuriProperties.Cookie cookie = matchuriProperties.getAuth().getCookie();
        ResponseCookie responseCookie = ResponseCookie.from(cookie.getRefreshTokenCookieName(), refreshToken)
                .httpOnly(true)
                .secure(cookie.isSecure())
                .path(cookie.getPath())
                .sameSite(cookie.getSameSite())
                .domain(cookie.getDomain())
                .maxAge(cookie.getMaxAgeSeconds())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    public void clearRefreshToken(HttpServletResponse response) {
        MatchuriProperties.Cookie cookie = matchuriProperties.getAuth().getCookie();
        ResponseCookie responseCookie = ResponseCookie.from(cookie.getRefreshTokenCookieName(), "")
                .httpOnly(true)
                .secure(cookie.isSecure())
                .path(cookie.getPath())
                .sameSite(cookie.getSameSite())
                .domain(cookie.getDomain())
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    public Optional<String> resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        String cookieName = matchuriProperties.getAuth().getCookie().getRefreshTokenCookieName();
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }
}
