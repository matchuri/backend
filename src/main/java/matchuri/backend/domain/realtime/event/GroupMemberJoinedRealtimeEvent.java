package matchuri.backend.domain.realtime.event;

import java.time.LocalDateTime;

public record GroupMemberJoinedRealtimeEvent(
        Long groupId,
        Long memberId,
        String memberNickname,
        LocalDateTime joinedAt
) {
}
