package matchuri.backend.global.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.global.config.AsyncConfig;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnlinkClientEventListener {

    private final OAuth2Unlinker oAuth2Unlinker;

    @Async(AsyncConfig.OAUTH_UNLINK_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OAuthUnlinkEvent event) {
        try {
            oAuth2Unlinker.unlink(event.memberId());
        } catch (Exception e) {
            log.error("OAuth2 unlink failed. memberId={}", event.memberId(), e);
        }
    }
}
