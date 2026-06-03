package matchuri.backend.domain.realtime.event;

import java.time.LocalDateTime;
import java.util.List;

public record GroupDeletedRealtimeEvent(
        Long groupId,
        Long deletedByMemberId,
        List<Long> targetMemberIds,
        LocalDateTime deletedAt
) {
}
