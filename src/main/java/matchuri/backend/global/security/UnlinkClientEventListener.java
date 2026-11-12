package matchuri.backend.global.security;

import lombok.RequiredArgsConstructor;
import matchuri.backend.global.config.AsyncConfig;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UnlinkClientEventListener {

    private final OAuth2Unlinker oAuth2Unlinker;
    private final OAuthUnlinkFailureHandler failureHandler;

    @Async(AsyncConfig.OAUTH_UNLINK_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OAuthUnlinkEvent event) {
        Long memberId = event.memberId();
        try {
            oAuth2Unlinker.unlink(memberId);
        } catch (RuntimeException e) {
            failureHandler.handle(memberId, e);
        }
    }
}
