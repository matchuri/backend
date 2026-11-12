package matchuri.backend.global.security;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.repository.OAuthUnlinkRetryRow;
import matchuri.backend.domain.auth.service.OAuthUnlinkJobServiceImpl;
import matchuri.backend.global.config.AsyncConfig;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthUnlinkRetryScheduler {

    private final OAuthUnlinkJobServiceImpl jobService;
    private final OAuth2Unlinker unlinker;
    private final OAuthUnlinkFailureHandler failureHandler;

    @Scheduled(
            fixedDelayString = "${matchuri.auth.oauth-unlink.poll-interval:1m}",
            initialDelayString = "${matchuri.auth.oauth-unlink.poll-interval:1m}",
            scheduler = AsyncConfig.OAUTH_UNLINK_RETRY_SCHEDULER
    )
    public void retryDueJobs() {
        for (OAuthUnlinkRetryRow job : jobService.findDueJobs()) {
            if (!LocalDateTime.now(ZoneOffset.UTC).isBefore(job.purgeAt())) {
                continue;
            }

            try {
                retry(job);
            } catch (RuntimeException exception) {
                log.error("Failed to update OAuth2 unlink job. jobId={} memberId={} errorType={}",
                        job.jobId(), job.memberId(), exception.getClass().getSimpleName());
            }
        }
    }

    private void retry(OAuthUnlinkRetryRow job) {
        try {
            unlinker.unlink(job.memberId());
        } catch (RuntimeException failure) {
            failureHandler.handleRetryFailure(job.jobId(), failure);
            return;
        }
        jobService.completeRetry(job.jobId());
    }
}
