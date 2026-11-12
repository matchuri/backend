package matchuri.backend.global.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.service.OAuthUnlinkJobServiceImpl;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthUnlinkFailureHandler {

    private final OAuthUnlinkJobServiceImpl oAuthUnlinkJobService;

    public void handle(Long memberId, Exception e) {
        try {
            oAuthUnlinkJobService.recordFailureJob(memberId, e.getClass().getName(), e.getMessage());
        } catch (Exception recordException) {
            recordException.addSuppressed(e);
            log.error("Failed to record OAuth2 unlink failure. memberId={}", memberId, recordException);
        }
    }
}
