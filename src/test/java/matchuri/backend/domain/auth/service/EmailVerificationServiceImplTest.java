package matchuri.backend.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.auth.command.SendEmailVerificationCommand;
import matchuri.backend.domain.auth.entity.EmailVerification;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.domain.auth.entity.EmailVerificationStatus;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.auth.repository.EmailVerificationRepository;
import matchuri.backend.domain.auth.support.mail.AuthMailSender;
import matchuri.backend.domain.auth.support.verification.EmailVerificationPolicy;
import matchuri.backend.domain.auth.support.verification.VerificationCodeGenerator;
import matchuri.backend.domain.auth.support.verification.VerificationCodeHasher;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    @Mock
    private EmailVerificationRepository repository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private VerificationCodeGenerator codeGenerator;

    @Mock
    private VerificationCodeHasher codeHasher;

    @Mock
    private EmailVerificationPolicy policy;

    @Mock
    private AuthMailSender authMailSender;

    @InjectMocks
    private EmailVerificationServiceImpl service;

    @Test
    @DisplayName("회원가입 이메일 인증 발송은 인증 코드를 해시로 저장하고 원문 코드는 메일 발송 경계에만 전달한다")
    void sendSignupVerificationEmailStoresCodeHash() {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 4, 26, 12, 5);
        when(repository.findAllByTargetAndStatus(
                "tester@example.com",
                EmailVerificationPurpose.SIGNUP,
                null,
                EmailVerificationStatus.PENDING
        )).thenReturn(List.of());
        when(memberRepository.existsByEmailAndSocialFalseAndStatus("tester@example.com", MemberStatus.ACTIVE))
                .thenReturn(false);
        when(codeGenerator.generateCode()).thenReturn("123456");
        when(codeHasher.hash("123456")).thenReturn("hashed-code");
        when(policy.codeExpiresAt(any(LocalDateTime.class))).thenReturn(expiresAt);
        when(policy.resendCooldownSeconds()).thenReturn(60L);

        var result = service.sendVerificationEmail(new SendEmailVerificationCommand(
                "TESTER@example.com ",
                EmailVerificationPurpose.SIGNUP,
                null
        ));

        ArgumentCaptor<EmailVerification> verificationCaptor = ArgumentCaptor.forClass(EmailVerification.class);
        verify(repository).save(verificationCaptor.capture());
        EmailVerification verification = verificationCaptor.getValue();
        assertThat(verification.getEmail()).isEqualTo("tester@example.com");
        assertThat(verification.getPurpose()).isEqualTo(EmailVerificationPurpose.SIGNUP);
        assertThat(verification.getCodeHash()).isEqualTo("hashed-code");
        assertThat(verification.getStatus()).isEqualTo(EmailVerificationStatus.PENDING);
        assertThat(verification.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(result.accepted()).isTrue();
        assertThat(result.resendAvailableAfterSeconds()).isEqualTo(60L);
        verify(authMailSender).sendVerificationEmail(
                "tester@example.com",
                EmailVerificationPurpose.SIGNUP,
                "123456"
        );
    }

    @Test
    @DisplayName("로그인 ID 찾기 발송은 계정이 없어도 성공 응답 모양을 유지하되 실제 메일은 보내지 않는다")
    void sendFindLoginIdEmailDoesNotRevealMissingAccount() {
        when(repository.findAllByTargetAndStatus(
                "missing@example.com",
                EmailVerificationPurpose.FIND_LOGIN_ID,
                null,
                EmailVerificationStatus.PENDING
        )).thenReturn(List.of());
        when(memberRepository.existsByEmailAndSocialFalseAndStatus("missing@example.com", MemberStatus.ACTIVE))
                .thenReturn(false);
        when(policy.resendCooldownSeconds()).thenReturn(60L);

        var result = service.sendVerificationEmail(new SendEmailVerificationCommand(
                "missing@example.com",
                EmailVerificationPurpose.FIND_LOGIN_ID,
                null
        ));

        assertThat(result.accepted()).isTrue();
        assertThat(result.resendAvailableAfterSeconds()).isEqualTo(60L);
        verify(repository, never()).save(any());
        verify(authMailSender, never()).sendVerificationEmail(any(), any(), any());
    }

    @Test
    @DisplayName("메일 발송 실패 시 인증 레코드를 실패 상태로 바꾸고 AUTH_EMAIL_SEND_FAILED로 변환한다")
    void sendEmailMarksVerificationFailedWhenMailSenderFails() {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 4, 26, 12, 5);
        when(repository.findAllByTargetAndStatus(
                "tester@example.com",
                EmailVerificationPurpose.SIGNUP,
                null,
                EmailVerificationStatus.PENDING
        )).thenReturn(List.of());
        when(memberRepository.existsByEmailAndSocialFalseAndStatus("tester@example.com", MemberStatus.ACTIVE))
                .thenReturn(false);
        when(codeGenerator.generateCode()).thenReturn("123456");
        when(codeHasher.hash("123456")).thenReturn("hashed-code");
        when(policy.codeExpiresAt(any(LocalDateTime.class))).thenReturn(expiresAt);
        org.mockito.Mockito.doThrow(new MailSendException("smtp unavailable"))
                .when(authMailSender)
                .sendVerificationEmail("tester@example.com", EmailVerificationPurpose.SIGNUP, "123456");

        assertThatThrownBy(() -> service.sendVerificationEmail(new SendEmailVerificationCommand(
                "tester@example.com",
                EmailVerificationPurpose.SIGNUP,
                null
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.EMAIL_SEND_FAILED);

        ArgumentCaptor<EmailVerification> verificationCaptor = ArgumentCaptor.forClass(EmailVerification.class);
        verify(repository).save(verificationCaptor.capture());
        assertThat(verificationCaptor.getValue().getStatus()).isEqualTo(EmailVerificationStatus.FAILED);
    }
}
