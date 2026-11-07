package matchuri.backend.infra.auth.captcha.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.auth.service.CaptchaPurpose;
import matchuri.backend.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GoogleRecaptchaVerifierTest {

    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private MockRestServiceServer server;
    private GoogleRecaptchaVerifier captchaVerifier;

    @BeforeEach
    void setUp() {
        GoogleRecaptchaProperties properties = new GoogleRecaptchaProperties();
        properties.setSecretKey("test-secret");
        properties.setVerifyUrl(VERIFY_URL);
        properties.setScoreThreshold(0.5);
        properties.setConnectTimeoutMillis(1000);
        properties.setReadTimeoutMillis(1000);

        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        captchaVerifier = new GoogleRecaptchaVerifier(properties, builder.build());
    }

    @Test
    @DisplayName("Google 응답의 success, action, score가 모두 유효하면 CAPTCHA 검증에 성공한다")
    void verifiesValidToken() {
        server.expect(requestTo(VERIFY_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(allOf(
                        containsString("secret=test-secret"),
                        containsString("response=test-token"),
                        containsString("remoteip=127.0.0.1")
                )))
                .andRespond(withSuccess("""
                        {
                          "success": true,
                          "score": 0.9,
                          "action": "login",
                          "hostname": "www.matchuri.com"
                        }
                        """, MediaType.APPLICATION_JSON));

        boolean verified = captchaVerifier.verify("test-token", CaptchaPurpose.LOGIN, "127.0.0.1");

        assertThat(verified).isTrue();
        server.verify();
    }

    @Test
    @DisplayName("Google 응답의 action이 다르면 CAPTCHA 검증을 거절한다")
    void rejectsUnexpectedAction() {
        respondWithVerification(0.9, "signup");

        assertThat(captchaVerifier.verify("test-token", CaptchaPurpose.LOGIN, "127.0.0.1")).isFalse();
    }

    @Test
    @DisplayName("Google 응답의 score가 임계값보다 낮으면 CAPTCHA 검증을 거절한다")
    void rejectsLowScore() {
        respondWithVerification(0.49, "login");

        assertThat(captchaVerifier.verify("test-token", CaptchaPurpose.LOGIN, "127.0.0.1")).isFalse();
    }

    @Test
    @DisplayName("Google이 토큰을 거절하면 CAPTCHA 검증 실패로 처리한다")
    void rejectsInvalidToken() {
        server.expect(requestTo(VERIFY_URL))
                .andRespond(withSuccess("""
                        {
                          "success": false,
                          "error-codes": ["timeout-or-duplicate"]
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(captchaVerifier.verify("test-token", CaptchaPurpose.LOGIN, "127.0.0.1")).isFalse();
    }

    @Test
    @DisplayName("Google secret 설정 오류는 CAPTCHA 서비스 장애로 처리한다")
    void treatsInvalidSecretAsServiceUnavailable() {
        server.expect(requestTo(VERIFY_URL))
                .andRespond(withSuccess("""
                        {
                          "success": false,
                          "error-codes": ["invalid-input-secret"]
                        }
                        """, MediaType.APPLICATION_JSON));

        assertServiceUnavailable();
    }

    @Test
    @DisplayName("Google 통신 오류는 CAPTCHA 서비스 장애로 처리한다")
    void treatsProviderFailureAsServiceUnavailable() {
        server.expect(requestTo(VERIFY_URL))
                .andRespond(withException(new IOException("connection failed")));

        assertServiceUnavailable();
    }

    private void respondWithVerification(double score, String action) {
        server.expect(requestTo(VERIFY_URL))
                .andRespond(withSuccess("""
                        {
                          "success": true,
                          "score": %s,
                          "action": "%s"
                        }
                        """.formatted(score, action), MediaType.APPLICATION_JSON));
    }

    private void assertServiceUnavailable() {
        assertThatThrownBy(() -> captchaVerifier.verify("test-token", CaptchaPurpose.LOGIN, "127.0.0.1"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.CAPTCHA_SERVICE_UNAVAILABLE));
    }
}
