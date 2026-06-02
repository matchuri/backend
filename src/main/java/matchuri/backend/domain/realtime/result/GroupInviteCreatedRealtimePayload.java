package matchuri.backend.domain.realtime.result;

import java.time.LocalDateTime;

public record GroupInviteCreatedRealtimePayload(
        Long inviteId,
        Long groupId,
        String groupName,
        Long requestMemberId,
        String requestMemberNickname,
        LocalDateTime expiresAt
) {
}
