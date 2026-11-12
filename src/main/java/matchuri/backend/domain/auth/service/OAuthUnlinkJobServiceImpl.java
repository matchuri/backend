package matchuri.backend.domain.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.entity.OAuthUnlinkJob;
import matchuri.backend.domain.auth.entity.FailureCode;
import matchuri.backend.domain.auth.entity.Status;
import matchuri.backend.domain.auth.repository.OAuthUnlinkJobRepository;
import matchuri.backend.domain.auth.repository.OAuthUnlinkRetryRow;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.support.member.MemberReader;
import matchuri.backend.global.config.MatchuriProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuthUnlinkJobServiceImpl {

    private static final int BATCH_SIZE = 20;

    private final MemberReader memberReader;
    private final OAuthUnlinkJobRepository jobRepository;
    private final MatchuriProperties properties;

    @Transactional
    public void recordFailureJob(Long memberId, FailureCode code, Integer httpStatus) {
        Member member = memberReader.get(memberId);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (member.getPurgeAt() == null || !now.isBefore(member.getPurgeAt())
                || jobRepository.findByMemberId(memberId).isPresent()) {
            return;
        }

        LocalDateTime nextAttemptAt = nextAttemptAt(1, member.getPurgeAt(), code, now);
        OAuthUnlinkJob job = jobRepository.save(OAuthUnlinkJob.firstFailure(member, code, httpStatus, nextAttemptAt));
        logFinalFailure(job);
    }

    @Transactional(readOnly = true)
    public List<OAuthUnlinkRetryRow> findDueJobs() {
        return jobRepository.findDueJobs(Status.PENDING, LocalDateTime.now(ZoneOffset.UTC),
                PageRequest.of(0, BATCH_SIZE));
    }

    @Transactional
    public void recordRetryFailure(Long jobId, FailureCode code, Integer httpStatus) {
        jobRepository.findById(jobId).ifPresent(job -> {
            LocalDateTime nextAttemptAt = nextAttemptAt(job.getAttemptCount() + 1, job.getMember().getPurgeAt(),
                    code, LocalDateTime.now(ZoneOffset.UTC));
            job.recordFailure(code, httpStatus, nextAttemptAt);
            logFinalFailure(job);
        });
    }

    @Transactional
    public void completeRetry(Long jobId) {
        jobRepository.deleteById(jobId);
    }

    private LocalDateTime nextAttemptAt(int attemptCount, LocalDateTime purgeAt, FailureCode code, LocalDateTime now) {
        List<Duration> delays = properties.getAuth().getOauthUnlink().getRetryDelays();
        if (!code.isRetryable() || attemptCount > delays.size()) {
            return null;
        }

        LocalDateTime next = now.plus(delays.get(attemptCount - 1));
        return next.isBefore(purgeAt) ? next : null;
    }

    private void logFinalFailure(OAuthUnlinkJob job) {
        if (job.getStatus() == Status.FAILED) {
            log.error("OAuth2 unlink retries ended. jobId={} memberId={} attempts={} failureCode={} httpStatus={}",
                    job.getId(), job.getMember().getId(), job.getAttemptCount(),
                    job.getLastFailureCode(), job.getLastHttpStatus());
        }
    }
}
