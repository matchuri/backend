package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupInviteStatus;

public record GroupInviteSummaryResult(
        Long inviteId,
        Long groupId,
        String groupName,
        Long requestMemberId,
        String requestMemberNickname,
        GroupInviteStatus status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
}
