package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupInviteStatus;
import matchuri.backend.domain.group.entity.GroupMemberStatus;

public record RespondGroupInviteResult(
        Long inviteId,
        Long groupId,
        GroupInviteStatus inviteStatus,
        GroupMemberStatus memberStatus,
        LocalDateTime respondedAt
) {
}
