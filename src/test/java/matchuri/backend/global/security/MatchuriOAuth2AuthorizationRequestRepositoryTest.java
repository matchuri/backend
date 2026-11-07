package matchuri.backend.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

class MatchuriOAuth2AuthorizationRequestRepositoryTest {

    @Test
    @DisplayName("OAuth2 authorization request는 서버 세션에 저장돼 대형 쿠키를 만들지 않는다")
    void saveAuthorizationRequestUsesServerSideSessionStorage() {
        MatchuriOAuth2AuthorizationRequestRepository repository = new MatchuriOAuth2AuthorizationRequestRepository();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("client-id")
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .state("state-value")
                .authorizationRequestUri("https://accounts.google.com/o/oauth2/v2/auth?state=state-value")
                .build();

        repository.saveAuthorizationRequest(authorizationRequest, request, response);

        assertThat(request.getSession(false)).isNotNull();
        assertThat(response.getHeader("Set-Cookie")).isNull();
        MockHttpServletRequest callbackRequest = new MockHttpServletRequest();
        callbackRequest.setSession(request.getSession());
        callbackRequest.setParameter("state", "state-value");
        OAuth2AuthorizationRequest loaded = repository.loadAuthorizationRequest(callbackRequest);

        assertThat(loaded).isNotNull();
        assertThat(loaded.getState()).isEqualTo("state-value");
        assertThat(loaded.getClientId()).isEqualTo("client-id");

        repository.clearAuthorizationRequest(callbackRequest, new MockHttpServletResponse());
        assertThat(repository.loadAuthorizationRequest(callbackRequest)).isNull();
    }
}
