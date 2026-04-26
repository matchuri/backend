package matchuri.backend.domain.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.command.SendEmailVerificationCommand;
import matchuri.backend.domain.auth.entity.EmailVerification;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.domain.auth.entity.EmailVerificationStatus;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.auth.repository.EmailVerificationRepository;
import matchuri.backend.domain.auth.result.SendEmailVerificationResult;
import matchuri.backend.domain.auth.support.mail.AuthMailSender;
import matchuri.backend.domain.auth.support.verification.EmailVerificationPolicy;
import matchuri.backend.domain.auth.support.verification.VerificationCodeGenerator;
import matchuri.backend.domain.auth.support.verification.VerificationCodeHasher;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationRepository repository;
    private final MemberRepository memberRepository;
    private final VerificationCodeGenerator codeGenerator;
    private final VerificationCodeHasher codeHasher;
    private final EmailVerificationPolicy policy;
    private final AuthMailSender authMailSender;

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public SendEmailVerificationResult sendVerificationEmail(SendEmailVerificationCommand command) {
        LocalDateTime now = LocalDateTime.now();
        List<EmailVerification> pendingVerifications = repository.findAllByTargetAndStatus(
                command.email(),
                command.purpose(),
                command.loginId(),
                EmailVerificationStatus.PENDING
        );

        if (!shouldSend(command)) {
            expirePrevious(pendingVerifications);
            return SendEmailVerificationResult.accepted(policy.resendCooldownSeconds());
        }

        long resendCooldownRemainingSeconds = resendCooldownRemainingSeconds(pendingVerifications, now);
        if (resendCooldownRemainingSeconds > 0) {
            return SendEmailVerificationResult.accepted(resendCooldownRemainingSeconds);
        }

        expirePrevious(pendingVerifications);

        String code = codeGenerator.generateCode();
        EmailVerification emailVerification = EmailVerification.issue(
                command.email(),
                command.loginId(),
                command.purpose(),
                codeHasher.hash(code),
                policy.codeExpiresAt(now),
                now
        );
        repository.save(emailVerification);

        try {
            authMailSender.sendVerificationEmail(command.email(), command.purpose(), code);
            log.info("Email verification message sent: purpose={}, email={}",
                    command.purpose(), maskEmail(command.email()));
            return SendEmailVerificationResult.accepted(policy.resendCooldownSeconds());
        } catch (MailException e) {
            emailVerification.markFailed();
            log.warn("Email verification message failed: purpose={}, email={}",
                    command.purpose(), maskEmail(command.email()));
            throw new BusinessException(AuthErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private long resendCooldownRemainingSeconds(List<EmailVerification> pendingVerifications, LocalDateTime now) {
        return pendingVerifications.stream()
                .findFirst()
                .map(verification -> policy.secondsUntilResend(verification.getLastSentAt(), now))
                .orElse(0L);
    }

    private boolean shouldSend(SendEmailVerificationCommand command) {
        if (command.purpose() == EmailVerificationPurpose.SIGNUP) {
            return !memberRepository.existsByEmailAndSocialFalseAndStatus(command.email(), MemberStatus.ACTIVE);
        }
        if (command.purpose() == EmailVerificationPurpose.FIND_LOGIN_ID) {
            return memberRepository.existsByEmailAndSocialFalseAndStatus(command.email(), MemberStatus.ACTIVE);
        }
        if (command.purpose() == EmailVerificationPurpose.RESET_PASSWORD) {
            return memberRepository.existsByLoginIdAndEmailAndSocialFalseAndStatus(
                    command.loginId(),
                    command.email(),
                    MemberStatus.ACTIVE
            );
        }
        return false;
    }

    private void expirePrevious(List<EmailVerification> pendingVerifications) {
        pendingVerifications.forEach(EmailVerification::expire);
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + email.substring(Math.max(atIndex, 0));
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
