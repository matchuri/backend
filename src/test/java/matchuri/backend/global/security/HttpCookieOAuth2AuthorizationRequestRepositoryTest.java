package matchuri.backend.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import matchuri.backend.global.config.MatchuriProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

    @Test
    @DisplayName("OAuth2 authorization request 쿠키는 설정된 정책으로 저장된다")
    void saveAuthorizationRequestUsesConfiguredCookiePolicy() {
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

        HttpCookieOAuth2AuthorizationRequestRepository repository =
                new HttpCookieOAuth2AuthorizationRequestRepository(properties);
        MockHttpServletResponse response = new MockHttpServletResponse();

        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("client-id")
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .state("state-value")
                .authorizationRequestUri("https://accounts.google.com/o/oauth2/v2/auth?state=state-value")
                .build();

        repository.saveAuthorizationRequest(authorizationRequest, new MockHttpServletRequest(), response);

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).contains("matchuri_oauth2_auth_request=");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Secure");
        assertThat(setCookie).contains("SameSite=None");
        assertThat(setCookie).contains("Domain=localhost");
        assertThat(setCookie).contains("Max-Age=180");
    }
}
