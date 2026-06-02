package matchuri.backend.domain.realtime.result;

import java.time.LocalDateTime;

public record RealtimeConnectedPayload(
        Long memberId,
        Long groupId,
        LocalDateTime connectedAt
) {
}
