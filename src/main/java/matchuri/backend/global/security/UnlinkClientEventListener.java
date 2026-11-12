package matchuri.backend.global.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.service.OAuthUnlinkJobServiceImpl;
import matchuri.backend.global.config.AsyncConfig;
import matchuri.backend.global.exception.MatchuriException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnlinkClientEventListener {

    private final OAuth2Unlinker oAuth2Unlinker;
    private final OAuthUnlinkJobServiceImpl oAuthUnlinkJobService;

    @Async(AsyncConfig.OAUTH_UNLINK_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OAuthUnlinkEvent event) {
        Long memberId = event.memberId();
        try {
            oAuth2Unlinker.unlink(memberId);
        } catch (MatchuriException e) {
            log.error("OAuth2 unlink failed (non-retryable). memberId={}", memberId, e);
        } catch (Exception e) {
            log.warn("OAuth2 unlink failed. memberId={} errorType={}", memberId, e.getClass().getName());
            recordFailure(memberId, e);
        }
    }

    private void recordFailure(Long memberId, Exception cause) {
        try {
            oAuthUnlinkJobService.recordFailureJob(memberId, cause.getClass().getName(), cause.getMessage());
        } catch (Exception recordException) {
            recordException.addSuppressed(cause);
            log.error("Failed to record OAuth2 unlink failure. memberId={}", memberId, recordException);
        }
    }
}
