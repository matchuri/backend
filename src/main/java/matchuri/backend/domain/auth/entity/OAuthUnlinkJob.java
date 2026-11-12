package matchuri.backend.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;
import matchuri.backend.domain.member.entity.Member;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@Table(
        name = "oauth_unlink_job",
        uniqueConstraints = @UniqueConstraint(name = "uk_oauth_unlink_job_member", columnNames = "member_id"),
        indexes = @Index(name = "idx_oauth_unlink_job_due", columnList = "status, next_attempt_at")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthUnlinkJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private Status status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_failure_code", nullable = false, length = 32)
    private FailureCode lastFailureCode;

    @Column(name = "last_http_status")
    private Integer lastHttpStatus;

    public static OAuthUnlinkJob firstFailure(
            Member member, FailureCode code, Integer httpStatus, LocalDateTime nextAttemptAt
    ) {
        OAuthUnlinkJob job = new OAuthUnlinkJob();
        job.member = member;
        job.recordFailure(code, httpStatus, nextAttemptAt);
        return job;
    }

    public void recordFailure(FailureCode code, Integer httpStatus, LocalDateTime nextAttemptAt) {
        attemptCount++;
        lastFailureCode = code;
        lastHttpStatus = httpStatus;
        this.nextAttemptAt = nextAttemptAt;
        status = nextAttemptAt == null ? Status.FAILED : Status.PENDING;
    }

}
