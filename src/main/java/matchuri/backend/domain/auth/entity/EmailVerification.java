package matchuri.backend.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;

@Getter
@Entity
@Table(
        name = "auth_email_verifications",
        indexes = {
                @Index(name = "idx_auth_email_verifications_email_purpose", columnList = "email,purpose"),
                @Index(name = "idx_auth_email_verifications_login_id", columnList = "login_id"),
                @Index(name = "idx_auth_email_verifications_status_expires_at", columnList = "status,expires_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_auth_email_verifications_token_hash",
                        columnNames = "verification_token_hash"
                )
        }
)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "login_id", length = 50)
    private String loginId;

    @Column(name = "purpose", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private EmailVerificationPurpose purpose;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EmailVerificationStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verification_token_hash")
    private String verificationTokenHash;

    @Column(name = "verification_token_expires_at")
    private LocalDateTime verificationTokenExpiresAt;

    @Column(name = "verification_token_used_at")
    private LocalDateTime verificationTokenUsedAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_sent_at", nullable = false)
    private LocalDateTime lastSentAt;

    public static EmailVerification issue(
            String email,
            String loginId,
            EmailVerificationPurpose purpose,
            String codeHash,
            LocalDateTime expiresAt,
            LocalDateTime lastSentAt
    ) {
        return new EmailVerification(
                null,
                email,
                loginId,
                purpose,
                codeHash,
                EmailVerificationStatus.PENDING,
                expiresAt,
                null,
                null,
                null,
                null,
                0,
                lastSentAt
        );
    }

    public void expire() {
        this.status = EmailVerificationStatus.EXPIRED;
    }

    public void markFailed() {
        this.status = EmailVerificationStatus.FAILED;
    }

    public void recordFailedAttempt(int maxAttempts) {
        this.attemptCount += 1;
        if (this.attemptCount >= maxAttempts) {
            markFailed();
        }
    }

    public void verify(String verificationTokenHash, LocalDateTime tokenExpiresAt, LocalDateTime verifiedAt) {
        this.status = EmailVerificationStatus.VERIFIED;
        this.verifiedAt = verifiedAt;
        this.verificationTokenHash = verificationTokenHash;
        this.verificationTokenExpiresAt = tokenExpiresAt;
    }

    public void markVerificationTokenUsed(LocalDateTime usedAt) {
        this.verificationTokenUsedAt = usedAt;
    }
}
