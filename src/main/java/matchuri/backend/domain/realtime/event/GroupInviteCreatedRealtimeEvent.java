package matchuri.backend.domain.realtime.event;

import java.time.LocalDateTime;

public record GroupInviteCreatedRealtimeEvent(
        Long inviteId,
        Long groupId,
        String groupName,
        Long requestMemberId,
        String requestMemberNickname,
        Long targetMemberId,
        LocalDateTime expiresAt
) {
}
