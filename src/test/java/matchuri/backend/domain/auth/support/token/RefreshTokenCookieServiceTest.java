package matchuri.backend.domain.auth.support.token;

import static org.assertj.core.api.Assertions.assertThat;

import matchuri.backend.global.config.MatchuriProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

class RefreshTokenCookieServiceTest {

    @Test
    @DisplayName("리프레시 토큰 쿠키는 설정된 보안 속성을 포함한다")
    void addRefreshTokenUsesConfiguredCookiePolicy() {
        MatchuriProperties properties = new MatchuriProperties();
        MatchuriProperties.Auth auth = new MatchuriProperties.Auth();
        MatchuriProperties.Cookie cookie = new MatchuriProperties.Cookie();
        cookie.setRefreshTokenCookieName("matchuri_refresh_token");
        cookie.setOauth2AuthorizationRequestCookieName("matchuri_oauth2_auth_request");
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setDomain("localhost");
        cookie.setSameSite("None");
        cookie.setMaxAgeSeconds(1209600);
        cookie.setOauth2AuthorizationRequestCookieMaxAgeSeconds(180);
        auth.setCookie(cookie);
        properties.setAuth(auth);

        RefreshTokenCookieService service = new RefreshTokenCookieService(properties);
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.addRefreshToken(response, "refresh-token");

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).contains("matchuri_refresh_token=refresh-token");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Secure");
        assertThat(setCookie).contains("SameSite=None");
        assertThat(setCookie).contains("Domain=localhost");
        assertThat(setCookie).contains("Path=/");
    }
}
