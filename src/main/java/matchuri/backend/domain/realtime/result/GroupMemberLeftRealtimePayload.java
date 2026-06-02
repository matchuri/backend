package matchuri.backend.domain.realtime.result;

import java.time.LocalDateTime;

public record GroupMemberLeftRealtimePayload(
        Long groupId,
        Long memberId,
        String memberNickname,
        LocalDateTime leftAt
) {
}
