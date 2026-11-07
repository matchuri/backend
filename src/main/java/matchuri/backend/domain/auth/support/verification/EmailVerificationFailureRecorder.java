package matchuri.backend.domain.auth.support.verification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.entity.EmailVerification;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.domain.auth.entity.EmailVerificationStatus;
import matchuri.backend.domain.auth.repository.EmailVerificationRepository;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.transaction.RollbackRecordExecutor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailVerificationFailureRecorder {

    private final EmailVerificationRepository repository;
    private final MemberRepository memberRepository;
    private final RollbackRecordExecutor rollbackRecordExecutor;

    public void expirePendingAfterRollback(List<EmailVerification> pendingVerifications) {
        List<Long> ids = pendingVerifications.stream().map(EmailVerification::getId).toList();
        rollbackRecordExecutor.afterRollback("email-verification-expiration", () -> expirePending(ids));
    }

    public void recordSendFailureAfterRollback(
            List<EmailVerification> pendingVerifications,
            EmailVerification failedVerification
    ) {
        List<Long> pendingIds = pendingVerifications.stream().map(EmailVerification::getId).toList();
        FailedDelivery delivery = FailedDelivery.from(failedVerification);
        rollbackRecordExecutor.afterRollback("email-verification-delivery-failure", () -> {
            expirePending(pendingIds);
            // Recreate the allowed record; never merge an entity mutated by the rolled-back transaction.
            EmailVerification failed = EmailVerification.issue(
                    delivery.email(), delivery.loginId(), delivery.purpose(), delivery.codeHash(),
                    delivery.expiresAt(), delivery.lastSentAt()
            );
            if (delivery.memberId() != null) {
                memberRepository.findById(delivery.memberId()).ifPresent(failed::assignMember);
            }
            failed.markFailed();
            repository.save(failed);
        });
    }

    public void expireAfterRollback(Long id) {
        updatePendingAfterRollback(id, "email-verification-expiration", EmailVerification::expire);
    }

    public void markFailedAfterRollback(Long id) {
        updatePendingAfterRollback(id, "email-verification-attempt-limit", EmailVerification::markFailed);
    }

    public void recordFailedAttemptAfterRollback(Long id, int maxAttempts) {
        updatePendingAfterRollback(id, "email-verification-failed-attempt",
                verification -> verification.recordFailedAttempt(maxAttempts));
    }

    private void updatePendingAfterRollback(Long id, String recordType, Consumer<EmailVerification> update) {
        rollbackRecordExecutor.afterRollback(recordType, () -> repository.findById(id)
                .filter(verification -> verification.getStatus() == EmailVerificationStatus.PENDING)
                .ifPresent(update));
    }

    private void expirePending(List<Long> ids) {
        repository.findAllById(ids).stream()
                .filter(verification -> verification.getStatus() == EmailVerificationStatus.PENDING)
                .forEach(EmailVerification::expire);
    }

    private record FailedDelivery(
            String email, String loginId, EmailVerificationPurpose purpose, String codeHash,
            LocalDateTime expiresAt, LocalDateTime lastSentAt, Long memberId
    ) {
        private static FailedDelivery from(EmailVerification verification) {
            return new FailedDelivery(
                    verification.getEmail(), verification.getLoginId(), verification.getPurpose(),
                    verification.getCodeHash(), verification.getExpiresAt(), verification.getLastSentAt(),
                    verification.getMember() == null ? null : verification.getMember().getId()
            );
        }
    }
}
