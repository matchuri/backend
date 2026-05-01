package matchuri.backend.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import matchuri.backend.domain.auth.entity.AuthRefreshToken;
import matchuri.backend.domain.auth.entity.EmailVerification;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.domain.auth.repository.AuthRefreshTokenRepository;
import matchuri.backend.domain.auth.repository.EmailVerificationRepository;
import matchuri.backend.domain.auth.support.verification.EmailVerificationTokenGenerator;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountRecoveryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private AuthRefreshTokenRepository authRefreshTokenRepository;

    @Autowired
    private EmailVerificationTokenGenerator emailVerificationTokenGenerator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        authRefreshTokenRepository.deleteAll();
        emailVerificationRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("로그인 ID 찾기 API는 FIND_LOGIN_ID token으로 단일 loginId를 반환하고 token을 사용 완료 처리한다")
    void findLoginId() throws Exception {
        memberRepository.save(new Member(
                "tester01",
                "hashed-password",
                "tester@example.com",
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        String token = issueFindLoginIdToken("tester@example.com");

        mockMvc.perform(post("/api/v1/auth/recovery/login-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "emailVerificationToken": "%s"
                                }
                                """.formatted(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loginId").value("tester01"));

        assertThat(emailVerificationRepository.findByVerificationTokenHash(
                emailVerificationTokenGenerator.hashToken(token)
        )).isPresent()
                .get()
                .extracting(EmailVerification::getVerificationTokenUsedAt)
                .isNotNull();
    }

    @Test
    @DisplayName("로그인 ID 찾기 API는 SIGNUP 목적 token을 거절한다")
    void findLoginIdRejectsSignupToken() throws Exception {
        String token = issueToken("tester@example.com", EmailVerificationPurpose.SIGNUP);

        mockMvc.perform(post("/api/v1/auth/recovery/login-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "emailVerificationToken": "%s"
                                }
                                """.formatted(token)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_EMAIL_VERIFICATION_FAILED"));
    }

    @Test
    @DisplayName("로그인 ID 찾기 API는 같은 token 재사용을 거절한다")
    void findLoginIdRejectsUsedToken() throws Exception {
        memberRepository.save(new Member(
                "tester01",
                "hashed-password",
                "tester@example.com",
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        String token = issueFindLoginIdToken("tester@example.com");

        mockMvc.perform(post("/api/v1/auth/recovery/login-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "emailVerificationToken": "%s"
                                }
                                """.formatted(token)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/recovery/login-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "emailVerificationToken": "%s"
                                }
                                """.formatted(token)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_EMAIL_VERIFICATION_FAILED"));
    }

    @Test
    @DisplayName("비밀번호 재설정 API는 RESET_PASSWORD token으로 비밀번호를 교체하고 기존 refresh token을 모두 폐기한다")
    void resetPassword() throws Exception {
        Member member = memberRepository.save(new Member(
                "tester01",
                passwordEncoder.encode("OldP@ssw0rd!"),
                "tester@example.com",
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        authRefreshTokenRepository.save(AuthRefreshToken.issue(
                member,
                "old-refresh-token",
                LocalDateTime.now().plusDays(7)
        ));
        String token = issueResetPasswordToken("tester@example.com", "tester01");

        mockMvc.perform(post("/api/v1/auth/recovery/password")
                        .cookie(new Cookie("matchuri_refresh_token", "old-refresh-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "tester01",
                                  "emailVerificationToken": "%s",
                                  "newPassword": "N3wP@ssw0rd!"
                                }
                                """.formatted(token)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("matchuri_refresh_token=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reset").value(true));

        Member updatedMember = memberRepository.findByLoginId("tester01").orElseThrow();
        assertThat(passwordEncoder.matches("N3wP@ssw0rd!", updatedMember.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("OldP@ssw0rd!", updatedMember.getPasswordHash())).isFalse();
        assertThat(authRefreshTokenRepository.findByMemberId(member.getId())).isEmpty();
        assertThat(emailVerificationRepository.findByVerificationTokenHash(
                emailVerificationTokenGenerator.hashToken(token)
        )).isPresent()
                .get()
                .extracting(EmailVerification::getVerificationTokenUsedAt)
                .isNotNull();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("matchuri_refresh_token", "old-refresh-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_REFRESH_TOKEN_INVALID"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "tester01",
                                  "password": "OldP@ssw0rd!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_LOGIN_FAILED"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "tester01",
                                  "password": "N3wP@ssw0rd!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString());
    }

    @Test
    @DisplayName("비밀번호 재설정 API는 loginId가 token의 loginId와 다르면 거절한다")
    void resetPasswordRejectsLoginIdMismatch() throws Exception {
        memberRepository.save(new Member(
                "tester01",
                passwordEncoder.encode("OldP@ssw0rd!"),
                "tester@example.com",
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        String token = issueResetPasswordToken("tester@example.com", "tester01");

        mockMvc.perform(post("/api/v1/auth/recovery/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "other-user",
                                  "emailVerificationToken": "%s",
                                  "newPassword": "N3wP@ssw0rd!"
                                }
                                """.formatted(token)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_EMAIL_VERIFICATION_FAILED"));

        assertThat(emailVerificationRepository.findByVerificationTokenHash(
                emailVerificationTokenGenerator.hashToken(token)
        )).isPresent()
                .get()
                .extracting(EmailVerification::getVerificationTokenUsedAt)
                .isNull();
    }

    private String issueFindLoginIdToken(String email) {
        return issueToken(email, EmailVerificationPurpose.FIND_LOGIN_ID);
    }

    private String issueResetPasswordToken(String email, String loginId) {
        return issueToken(email, loginId, EmailVerificationPurpose.RESET_PASSWORD);
    }

    private String issueToken(String email, EmailVerificationPurpose purpose) {
        return issueToken(email, null, purpose);
    }

    private String issueToken(String email, String loginId, EmailVerificationPurpose purpose) {
        String token = "ev_test_" + purpose.name().toLowerCase()
                + "_" + email.replace("@", "_").replace(".", "_")
                + (loginId == null ? "" : "_" + loginId);
        EmailVerification verification = EmailVerification.issue(
                email,
                loginId,
                purpose,
                "hashed-code",
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now()
        );
        verification.verify(
                emailVerificationTokenGenerator.hashToken(token),
                LocalDateTime.now().plusMinutes(10),
                LocalDateTime.now()
        );
        emailVerificationRepository.save(verification);
        return token;
    }
}
