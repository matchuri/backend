package matchuri.backend.domain.auth.repository;

import java.time.LocalDateTime;

public record OAuthUnlinkRetryRow(
        Long jobId,
        Long memberId,
        LocalDateTime purgeAt
) {
}
