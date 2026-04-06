package matchuri.backend.global.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import matchuri.backend.global.config.MatchuriProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.util.SerializationUtils;

public final class CookieUtils {

    private CookieUtils() {
    }

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .findFirst();
    }

    public static void addCookie(
            HttpServletResponse response,
            String name,
            String value,
            int maxAge,
            MatchuriProperties.Cookie cookieConfig
    ) {
        ResponseCookie responseCookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieConfig.isSecure())
                .path(cookieConfig.getPath())
                .domain(cookieConfig.getDomain())
                .sameSite(cookieConfig.getSameSite())
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    public static void deleteCookie(
            HttpServletResponse response,
            String name,
            MatchuriProperties.Cookie cookieConfig
    ) {
        ResponseCookie responseCookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieConfig.isSecure())
                .path(cookieConfig.getPath())
                .domain(cookieConfig.getDomain())
                .sameSite(cookieConfig.getSameSite())
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    public static String serialize(Object value) {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(value));
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        return (T) SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.getValue()));
    }
}
