package matchuri.backend.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import matchuri.backend.domain.auth.command.ConfirmEmailVerificationCommand;
import matchuri.backend.domain.auth.entity.EmailVerification;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.domain.auth.entity.EmailVerificationStatus;
import matchuri.backend.domain.auth.repository.EmailVerificationRepository;
import matchuri.backend.domain.auth.service.EmailVerificationService;
import matchuri.backend.domain.auth.support.mail.AuthMailSender;
import matchuri.backend.domain.auth.support.verification.EmailVerificationTokenGenerator;
import matchuri.backend.domain.auth.support.verification.VerificationCodeHasher;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.exception.AuthenticationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmailVerificationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmailVerificationRepository repository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private EmailVerificationService service;
    @Autowired private VerificationCodeHasher codeHasher;
    @Autowired private EmailVerificationTokenGenerator tokenGenerator;
    @Autowired private PlatformTransactionManager transactionManager;
    @MockitoBean private AuthMailSender mailSender;

    @BeforeEach
    @AfterEach
    void clearData() {
        repository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("발송 성공 응답의 계약을 유지하고 응답 전에 PENDING 인증을 저장한다")
    void sendSuccessKeepsContractAndPersistsBeforeResponse() throws Exception {
        mockMvc.perform(post("/api/v1/auth/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"tester@example.com","purpose":"SIGNUP"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"success":true,"data":{"accepted":true,"resendAvailableAfterSeconds":60},"error":null}
                        """, JsonCompareMode.STRICT));

        assertThat(repository.findAll()).singleElement().satisfies(verification -> {
            assertThat(verification.getStatus()).isEqualTo(EmailVerificationStatus.PENDING);
            assertThat(verification.getCodeHash()).isNotBlank();
            assertThat(verification.getVerificationTokenHash()).isNull();
        });
    }

    @Test
    @DisplayName("인증 성공은 기존 응답 필드를 유지하고 token hash 저장을 완료한 뒤 반환한다")
    void confirmSuccessKeepsContractAndPersistsToken() throws Exception {
        EmailVerification pending = issue("tester@example.com", null, EmailVerificationPurpose.SIGNUP,
                LocalDateTime.now().plusMinutes(5));

        String response = mockMvc.perform(post("/api/v1/auth/email/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmRequest("123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verified").value(true))
                .andExpect(jsonPath("$.data.expiresIn").value(600))
                .andReturn().getResponse().getContentAsString();
        JsonNode envelope = objectMapper.readTree(response);
        assertThat(envelope.properties()).extracting(java.util.Map.Entry::getKey)
                .containsExactlyInAnyOrder("success", "data", "error");
        JsonNode data = envelope.path("data");
        assertThat(data.properties()).extracting(java.util.Map.Entry::getKey)
                .containsExactlyInAnyOrder("verified", "emailVerificationToken", "expiresIn");
        String token = data.path("emailVerificationToken").asText();
        assertThat(token).isNotBlank();
        assertThat(envelope.path("error").isNull()).isTrue();
        EmailVerification stored = repository.findById(pending.getId()).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(EmailVerificationStatus.VERIFIED);
        assertThat(stored.getVerificationTokenHash()).isEqualTo(tokenGenerator.hashToken(token)).isNotEqualTo(token);
    }

    @Test
    @DisplayName("메일 실패는 업무 변경을 롤백하고 이전 인증 만료와 새 FAILED 기록만 보존한다")
    void mailFailurePreservesOnlyAllowedRecords() throws Exception {
        Member member = memberRepository.save(new Member("tester01", "hashed-password", "tester@example.com",
                false, null, null, MemberRole.MEMBER, MemberStatus.ACTIVE));
        member.updateNickname("원래닉네임");
        memberRepository.save(member);
        EmailVerification previous = issue("tester@example.com", "tester01", EmailVerificationPurpose.RESET_PASSWORD,
                LocalDateTime.now().plusMinutes(5));
        doAnswer(invocation -> {
            memberRepository.findById(member.getId()).orElseThrow().updateNickname("롤백할닉네임");
            throw new MailSendException("smtp unavailable");
        }).when(mailSender).sendVerificationEmail(any(), any(), any());

        mockMvc.perform(post("/api/v1/auth/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"tester@example.com","purpose":"RESET_PASSWORD","loginId":"tester01"}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value("AUTH_EMAIL_SEND_FAILED"));

        assertThat(memberRepository.findById(member.getId()).orElseThrow().getNickname()).isEqualTo("원래닉네임");
        assertThat(repository.findById(previous.getId()).orElseThrow().getStatus())
                .isEqualTo(EmailVerificationStatus.EXPIRED);
        assertThat(repository.findAll()).filteredOn(v -> v.getStatus() == EmailVerificationStatus.FAILED)
                .singleElement().satisfies(failed -> {
                    assertThat(failed.getMember().getId()).isEqualTo(member.getId());
                    assertThat(failed.getCodeHash()).isNotBlank();
                    assertThat(failed.getVerificationTokenHash()).isNull();
                });
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("인증 오류 응답을 유지하면서 실패 횟수 5회와 FAILED 상태를 보존한다")
    void wrongCodePreservesAttemptsAndLimit() throws Exception {
        EmailVerification pending = issue("tester@example.com", null, EmailVerificationPurpose.SIGNUP,
                LocalDateTime.now().plusMinutes(5));
        for (int attempt = 1; attempt <= 5; attempt++) {
            mockMvc.perform(post("/api/v1/auth/email/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(confirmRequest("654321")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().json("""
                            {"success":false,"data":null,"error":{"status":401,
                            "code":"AUTH_EMAIL_VERIFICATION_FAILED","message":"이메일 인증에 실패했습니다.","details":[]}}
                            """, JsonCompareMode.STRICT));
            assertThat(repository.findById(pending.getId()).orElseThrow().getAttemptCount()).isEqualTo(attempt);
        }
        assertThat(repository.findById(pending.getId()).orElseThrow().getStatus()).isEqualTo(EmailVerificationStatus.FAILED);
        mockMvc.perform(post("/api/v1/auth/email/confirm").contentType(MediaType.APPLICATION_JSON)
                        .content(confirmRequest("123456")))
                .andExpect(status().isUnauthorized());
        assertThat(repository.findById(pending.getId()).orElseThrow().getVerificationTokenHash()).isNull();
    }

    @Test
    @DisplayName("만료된 인증은 실패 응답 이후에도 EXPIRED로 보존한다")
    void expiredCodePreservesExpiration() throws Exception {
        EmailVerification pending = issue("tester@example.com", null, EmailVerificationPurpose.SIGNUP,
                LocalDateTime.now().minusSeconds(1));
        mockMvc.perform(post("/api/v1/auth/email/confirm").contentType(MediaType.APPLICATION_JSON)
                        .content(confirmRequest("123456")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_EMAIL_VERIFICATION_FAILED"));
        assertThat(repository.findById(pending.getId()).orElseThrow().getStatus()).isEqualTo(EmailVerificationStatus.EXPIRED);
    }

    @Test
    @DisplayName("중복 이메일 오류는 이전 미완료 인증만 만료시키고 새 인증이나 메일을 만들지 않는다")
    void duplicateEmailExpiresPreviousWithoutIssuingCode() throws Exception {
        memberRepository.save(new Member("tester01", "hashed-password", "tester@example.com",
                false, null, null, MemberRole.MEMBER, MemberStatus.ACTIVE));
        EmailVerification pending = issue("tester@example.com", null, EmailVerificationPurpose.SIGNUP,
                LocalDateTime.now().plusMinutes(5));
        mockMvc.perform(post("/api/v1/auth/email").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"tester@example.com","purpose":"SIGNUP"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("MEMBER_DUPLICATE_EMAIL"));
        assertThat(repository.findById(pending.getId()).orElseThrow().getStatus()).isEqualTo(EmailVerificationStatus.EXPIRED);
        assertThat(repository.count()).isEqualTo(1);
        verifyNoInteractions(mailSender);
    }

    @Test
    @DisplayName("외부 유스케이스 트랜잭션도 인증 실패 시 함께 롤백하고 실패 횟수만 보존한다")
    void joinedTransactionRollsBackBusinessChanges() {
        Member member = memberRepository.save(new Member("tester01", "hashed-password", "tester@example.com",
                false, null, null, MemberRole.MEMBER, MemberStatus.ACTIVE));
        EmailVerification pending = issue("tester@example.com", null, EmailVerificationPurpose.SIGNUP,
                LocalDateTime.now().plusMinutes(5));
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            memberRepository.findById(member.getId()).orElseThrow().updateNickname("롤백할닉네임");
            service.confirmVerificationEmail(new ConfirmEmailVerificationCommand(
                    "tester@example.com", EmailVerificationPurpose.SIGNUP, null, "654321"));
        })).isInstanceOf(AuthenticationException.class);
        assertThat(memberRepository.findById(member.getId()).orElseThrow().getNickname()).isNull();
        assertThat(repository.findById(pending.getId()).orElseThrow().getAttemptCount()).isEqualTo(1);
    }

    private EmailVerification issue(String email, String loginId, EmailVerificationPurpose purpose, LocalDateTime expiresAt) {
        return repository.save(EmailVerification.issue(email, loginId, purpose, codeHasher.hash("123456"),
                expiresAt, LocalDateTime.now().minusMinutes(2)));
    }

    private String confirmRequest(String code) {
        return """
                {"email":"tester@example.com","purpose":"SIGNUP","code":"%s"}
                """.formatted(code);
    }
}
