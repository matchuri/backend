package matchuri.backend.domain.realtime.result;

import java.time.LocalDateTime;

public record GroupDeletedRealtimePayload(
        Long groupId,
        Long deletedByMemberId,
        LocalDateTime deletedAt
) {
}
