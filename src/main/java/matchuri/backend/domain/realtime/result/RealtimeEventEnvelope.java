package matchuri.backend.domain.realtime.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.realtime.entity.RealtimeEventType;

public record RealtimeEventEnvelope(
        String eventId,
        RealtimeEventType eventType,
        LocalDateTime occurredAt,
        Long groupId,
        Long sessionId,
        Long actorMemberId,
        Object payload
) {
}
