package matchuri.backend.domain.realtime.event;

import java.time.LocalDateTime;

public record GroupMemberLeftRealtimeEvent(
        Long groupId,
        Long ownerMemberId,
        Long memberId,
        String memberNickname,
        LocalDateTime leftAt
) {
}
