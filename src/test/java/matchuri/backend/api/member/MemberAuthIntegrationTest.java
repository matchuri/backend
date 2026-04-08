package matchuri.backend.api.member;

import static org.hamcrest.Matchers.nullValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import javax.crypto.SecretKey;
import matchuri.backend.domain.auth.entity.AuthExchangeCode;
import matchuri.backend.domain.auth.repository.AuthExchangeCodeRepository;
import matchuri.backend.domain.auth.repository.AuthRefreshTokenRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import matchuri.backend.global.config.MatchuriProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import io.jsonwebtoken.security.Keys;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthRefreshTokenRepository authRefreshTokenRepository;

    @Autowired
    private AuthExchangeCodeRepository authExchangeCodeRepository;

    @Autowired
    private MemberTasteProfileRepository memberTasteProfileRepository;

    @Autowired
    private MatchuriProperties matchuriProperties;

    @BeforeEach
    void setUp() {
        authExchangeCodeRepository.deleteAll();
        authRefreshTokenRepository.deleteAll();
        memberTasteProfileRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원 가입 시 비밀번호는 해시로 저장되고 생성 응답을 반환한다")
    void createMember() throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "tester01",
                                  "password": "P@ssw0rd!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loginId").value("tester01"))
                .andExpect(jsonPath("$.data.memberId").isNumber());

        Member member = memberRepository.findByLoginId("tester01").orElseThrow();
        assertThat(member.getPasswordHash()).isNotEqualTo("P@ssw0rd!");
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("중복 로그인 아이디로 가입하면 MEMBER_DUPLICATE_LOGIN_ID를 반환한다")
    void createMemberWithDuplicateLoginId() throws Exception {
        memberRepository.save(new Member(
                "tester01",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "tester01",
                                  "password": "P@ssw0rd!"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MEMBER_DUPLICATE_LOGIN_ID"));
    }

    @Test
    @DisplayName("로그아웃은 refresh token만 폐기하고 access token은 만료 전까지 유지된다")
    void memberAuthLifecycle() throws Exception {
        createMemberThroughApi("tester01", "P@ssw0rd!");
        AuthSession authSession = login("tester01", "P@ssw0rd!");
        String accessToken = authSession.accessToken();

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.nickname").value(nullValue()))
                .andExpect(jsonPath("$.data.loginId").doesNotExist())
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.memberTasteProfile").doesNotExist());

        mockMvc.perform(patch("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "점심탐험가"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.updatedAt").exists());

        mockMvc.perform(patch("/api/v1/members/me/taste-profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profileVersion": "v1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.updatedAt").exists());

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.nickname").value("점심탐험가"))
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.memberTasteProfile").doesNotExist());

        assertThat(memberTasteProfileRepository.findByMemberId(memberRepository.findByLoginId("tester01").orElseThrow().getId()))
                .isPresent()
                .get()
                .extracting(profile -> profile.getProfileVersion())
                .isEqualTo("v1");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .cookie(authSession.refreshTokenCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loggedOut").value(true))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        assertThat(authRefreshTokenRepository.findByMemberId(memberRepository.findByLoginId("tester01").orElseThrow().getId()))
                .isEmpty();

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.nickname").value("점심탐험가"));

        mockMvc.perform(delete("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("MEMBER_INACTIVE_MEMBER"));
    }

    @Test
    @DisplayName("보호 API는 토큰 없이 접근하면 AUTH_TOKEN_MISSING을 반환한다")
    void protectedApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_TOKEN_MISSING"));
    }

    @Test
    @DisplayName("만료된 access token으로 보호 API에 접근하면 AUTH_TOKEN_EXPIRED를 반환한다")
    void protectedApiRejectsExpiredAccessToken() throws Exception {
        createMemberThroughApi("expired-user", "P@ssw0rd!");
        Member member = memberRepository.findByLoginId("expired-user").orElseThrow();
        String expiredAccessToken = expiredAccessToken(member);

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(expiredAccessToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_TOKEN_EXPIRED"));
    }

    @Test
    @DisplayName("로그아웃 API는 토큰 없이 접근하면 AUTH_TOKEN_MISSING을 반환한다")
    void logoutRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_TOKEN_MISSING"));
    }

    @Test
    @DisplayName("로그아웃 시 refresh token 쿠키가 없으면 AUTH_LOGOUT_FAILED를 반환한다")
    void logoutFailsWhenRefreshTokenCookieIsMissing() throws Exception {
        createMemberThroughApi("logout-user", "P@ssw0rd!");
        AuthSession authSession = login("logout-user", "P@ssw0rd!");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("AUTH_LOGOUT_FAILED"));
    }

    @Test
    @DisplayName("구글 OAuth2 시작 요청은 Spring Security authorization 엔드포인트로 리다이렉트한다")
    void startGoogleOAuth2LoginRedirects() throws Exception {
        mockMvc.perform(get("/api/v1/auth/oauth2/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, "/oauth2/authorization/google"));
    }

    @Test
    @DisplayName("유효한 소셜 교환 코드는 액세스 토큰으로 교환된다")
    void exchangeOAuth2Code() throws Exception {
        Member member = memberRepository.save(Member.createSocialMember(SocialProviderType.GOOGLE, "google-user-1", "google@example.com", "구글사용자"));
        authExchangeCodeRepository.save(AuthExchangeCode.issue(
                member,
                SocialProviderType.GOOGLE,
                "valid-exchange-code",
                LocalDateTime.now().plusMinutes(5)
        ));

        mockMvc.perform(post("/api/v1/auth/oauth2/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "GOOGLE",
                                  "code": "valid-exchange-code"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").value(nullValue()))
                .andExpect(jsonPath("$.data.member.id").value(member.getId()));
    }

    @Test
    @DisplayName("소셜 교환 코드는 한 번 사용 후 재사용할 수 없다")
    void exchangeOAuth2CodeCannotBeReused() throws Exception {
        Member member = memberRepository.save(Member.createSocialMember(SocialProviderType.GOOGLE, "google-user-2", "google2@example.com", "구글사용자2"));
        authExchangeCodeRepository.save(AuthExchangeCode.issue(
                member,
                SocialProviderType.GOOGLE,
                "single-use-code",
                LocalDateTime.now().plusMinutes(5)
        ));

        mockMvc.perform(post("/api/v1/auth/oauth2/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "GOOGLE",
                                  "code": "single-use-code"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString());

        mockMvc.perform(post("/api/v1/auth/oauth2/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "GOOGLE",
                                  "code": "single-use-code"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_OAUTH2_EXCHANGE_CODE_INVALID"));
    }

    @Test
    @DisplayName("만료된 소셜 교환 코드는 AUTH_OAUTH2_EXCHANGE_CODE_INVALID를 반환한다")
    void exchangeOAuth2CodeFailsWhenCodeIsExpired() throws Exception {
        Member member = memberRepository.save(Member.createSocialMember(SocialProviderType.GOOGLE, "google-user-3", "google3@example.com", "구글사용자3"));
        authExchangeCodeRepository.save(AuthExchangeCode.issue(
                member,
                SocialProviderType.GOOGLE,
                "expired-exchange-code",
                LocalDateTime.now().minusMinutes(1)
        ));

        mockMvc.perform(post("/api/v1/auth/oauth2/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "GOOGLE",
                                  "code": "expired-exchange-code"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_OAUTH2_EXCHANGE_CODE_INVALID"));
    }

    @Test
    @DisplayName("유효하지 않은 소셜 교환 코드는 AUTH_OAUTH2_EXCHANGE_CODE_INVALID를 반환한다")
    void exchangeOAuth2CodeFailsWhenCodeIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/oauth2/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "GOOGLE",
                                  "code": "missing-code"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_OAUTH2_EXCHANGE_CODE_INVALID"));
    }

    @Test
    @DisplayName("탈퇴한 회원은 다시 로컬 로그인할 수 없다")
    void withdrawnMemberCannotLoginAgain() throws Exception {
        createMemberThroughApi("withdrawn-user", "P@ssw0rd!");
        AuthSession authSession = login("withdrawn-user", "P@ssw0rd!");

        mockMvc.perform(delete("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "withdrawn-user",
                                  "password": "P@ssw0rd!"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("MEMBER_INACTIVE_MEMBER"));
    }

    @Test
    @DisplayName("허용된 Origin의 preflight 요청은 CORS 헤더를 반환한다")
    void preflightReturnsCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    private void createMemberThroughApi(String loginId, String password) throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "%s",
                                  "password": "%s"
                                }
                                """.formatted(loginId, password)))
                .andExpect(status().isOk());
    }

    private AuthSession login(String loginId, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "%s",
                                  "password": "%s"
                                }
                                """.formatted(loginId, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").value(nullValue()))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("matchuri_refresh_token=")))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return new AuthSession(
                body.path("data").path("accessToken").asText(),
                result.getResponse().getCookie("matchuri_refresh_token")
        );
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private String expiredAccessToken(Member member) {
        Instant now = Instant.now();
        Instant issuedAt = now.minusSeconds(3600);
        Instant expiredAt = now.minusSeconds(1800);
        SecretKey signingKey = Keys.hmacShaKeyFor(
                matchuriProperties.getAuth().getJwt().getSecret().getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
                .issuer(matchuriProperties.getAuth().getJwt().getIssuer())
                .subject(String.valueOf(member.getId()))
                .claim("role", member.getMemberRole().name())
                .claim("loginId", member.getLoginId())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiredAt))
                .signWith(signingKey)
                .compact();
    }

    private record AuthSession(
            String accessToken,
            Cookie refreshTokenCookie
    ) {
    }
}
