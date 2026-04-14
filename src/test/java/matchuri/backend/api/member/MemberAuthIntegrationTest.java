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
import matchuri.backend.domain.auth.entity.AuthRefreshToken;
import matchuri.backend.domain.auth.entity.AuthExchangeCode;
import matchuri.backend.domain.auth.repository.AuthExchangeCodeRepository;
import matchuri.backend.domain.auth.repository.AuthRefreshTokenRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
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
    private MemberAgreementRepository memberAgreementRepository;

    @Autowired
    private MatchuriProperties matchuriProperties;

    @BeforeEach
    void setUp() {
        authExchangeCodeRepository.deleteAll();
        authRefreshTokenRepository.deleteAll();
        memberAgreementRepository.deleteAll();
        memberTasteProfileRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("자체 회원가입 통합 API는 약관과 닉네임을 함께 저장하지만 로그인 상태는 만들지 않는다")
    void registerLocalMember() throws Exception {
        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "signup-user",
                                  "password": "P@ssw0rd!",
                                  "nickname": "점심탐험가",
                                  "agreements": [
                                    {
                                      "agreementType": "TERMS_OF_SERVICE",
                                      "agreementVersion": "2026-04-10"
                                    },
                                    {
                                      "agreementType": "PRIVACY_POLICY",
                                      "agreementVersion": "2026-04-10"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loginId").value("signup-user"))
                .andExpect(jsonPath("$.data.nickname").value("점심탐험가"))
                .andExpect(jsonPath("$.data.memberId").isNumber())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        Member member = memberRepository.findByLoginId("signup-user").orElseThrow();
        assertThat(member.getPasswordHash()).isNotEqualTo("P@ssw0rd!");
        assertThat(member.getNickname()).isEqualTo("점심탐험가");
        assertThat(memberAgreementRepository.count()).isEqualTo(2);

        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_TOKEN_MISSING"));

        AuthSession authSession = login("signup-user", "P@ssw0rd!");
        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authSession.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("점심탐험가"));
    }

    @Test
    @DisplayName("자체 회원가입 통합 API에서 약관 검증이 실패하면 회원과 약관 기록이 남지 않는다")
    void registerLocalMemberRollsBackWhenAgreementValidationFails() throws Exception {
        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "rollback-user",
                                  "password": "P@ssw0rd!",
                                  "nickname": "롤백검증",
                                  "agreements": [
                                    {
                                      "agreementType": "TERMS_OF_SERVICE",
                                      "agreementVersion": "2026-03-01"
                                    },
                                    {
                                      "agreementType": "PRIVACY_POLICY",
                                      "agreementVersion": "2026-04-10"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("MEMBER_AGREEMENT_VERSION_MISMATCH"));

        assertThat(memberRepository.findByLoginId("rollback-user")).isEmpty();
        assertThat(memberAgreementRepository.count()).isZero();
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
        String accessToken = submitRequiredAgreements(authSession.accessToken());

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
                .extracting(MemberTasteProfile::getProfileVersion)
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
    @DisplayName("내 닉네임 수정 시 이미 존재하는 닉네임이면 MEMBER_DUPLICATE_NICKNAME을 반환한다")
    void updateMyProfileFailsWhenNicknameAlreadyExists() throws Exception {
        createMemberThroughApi("tester01", "P@ssw0rd!");
        createMemberThroughApi("tester02", "P@ssw0rd!");

        AuthSession authSession = login("tester01", "P@ssw0rd!");
        String accessToken = submitRequiredAgreements(authSession.accessToken());

        Member duplicateNicknameOwner = memberRepository.findByLoginId("tester02").orElseThrow();
        duplicateNicknameOwner.updateNickname("점심탐험가");
        memberRepository.saveAndFlush(duplicateNicknameOwner);

        mockMvc.perform(patch("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "점심탐험가"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("MEMBER_DUPLICATE_NICKNAME"));
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
    @DisplayName("refresh token 쿠키로 새 access token과 refresh token을 재발급한다")
    void refreshReissuesTokens() throws Exception {
        createMemberThroughApi("refresh-user", "P@ssw0rd!");
        AuthSession authSession = login("refresh-user", "P@ssw0rd!");
        Long memberId = memberRepository.findByLoginId("refresh-user").orElseThrow().getId();
        String previousRefreshToken = authSession.refreshTokenCookie().getValue();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(authSession.refreshTokenCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").value(nullValue()))
                .andExpect(jsonPath("$.data.member.id").value(memberId))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("matchuri_refresh_token=")))
                .andReturn();

        Cookie rotatedCookie = result.getResponse().getCookie("matchuri_refresh_token");
        assertThat(rotatedCookie).isNotNull();
        assertThat(rotatedCookie.getValue()).isNotEqualTo(previousRefreshToken);

        assertThat(authRefreshTokenRepository.findByToken(previousRefreshToken)).isEmpty();
        assertThat(authRefreshTokenRepository.findByMemberId(memberId))
                .hasSize(1)
                .extracting(AuthRefreshToken::getToken)
                .containsExactly(rotatedCookie.getValue());

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String refreshedAccessToken = body.path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(refreshedAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("MEMBER_AGREEMENT_REQUIRED"));
    }

    @Test
    @DisplayName("refresh token 쿠키가 없으면 AUTH_REFRESH_TOKEN_MISSING을 반환한다")
    void refreshFailsWhenRefreshTokenCookieIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REFRESH_TOKEN_MISSING"));
    }

    @Test
    @DisplayName("유효하지 않은 refresh token이면 쿠키를 비우고 AUTH_REFRESH_TOKEN_INVALID를 반환한다")
    void refreshFailsWhenRefreshTokenIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("matchuri_refresh_token", "missing-refresh-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REFRESH_TOKEN_INVALID"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));
    }

    @Test
    @DisplayName("만료된 refresh token이면 쿠키를 비우고 AUTH_REFRESH_TOKEN_EXPIRED를 반환한다")
    void refreshFailsWhenRefreshTokenIsExpired() throws Exception {
        Member member = memberRepository.save(new Member(
                "expired-refresh-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        authRefreshTokenRepository.save(AuthRefreshToken.issue(
                member,
                "expired-refresh-token",
                LocalDateTime.now().minusMinutes(1)
        ));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("matchuri_refresh_token", "expired-refresh-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REFRESH_TOKEN_EXPIRED"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));
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
        Member member = memberRepository.save(Member.createSocialMember(SocialProviderType.GOOGLE, "google-user-1", "google@example.com", "example_google"));
        authExchangeCodeRepository.save(AuthExchangeCode.issue(
                member,
                SocialProviderType.GOOGLE,
                "valid-exchange-code",
                LocalDateTime.now().plusMinutes(5)
        ));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/oauth2/exchange")
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
                .andExpect(jsonPath("$.data.member.id").value(member.getId()))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String accessToken = body.path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("MEMBER_AGREEMENT_REQUIRED"));
    }

    @Test
    @DisplayName("소셜 교환 코드는 한 번 사용 후 재사용할 수 없다")
    void exchangeOAuth2CodeCannotBeReused() throws Exception {
        Member member = memberRepository.save(Member.createSocialMember(SocialProviderType.GOOGLE, "google-user-2", "google2@example.com", "google2_google"));
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
        Member member = memberRepository.save(Member.createSocialMember(SocialProviderType.GOOGLE, "google-user-3", "google3@example.com", "google3_google"));
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
        String accessToken = submitRequiredAgreements(authSession.accessToken());

        mockMvc.perform(delete("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
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

    private String submitRequiredAgreements(String accessToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/member-agreements/consents")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "agreements": [
                                    {
                                      "agreementType": "TERMS_OF_SERVICE",
                                      "agreementVersion": "2026-04-10"
                                    },
                                    {
                                      "agreementType": "PRIVACY_POLICY",
                                      "agreementVersion": "2026-04-10"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.path("data").path("accessToken").asText();
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
                .claim("requiredAgreementRevision", null)
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
