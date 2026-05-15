package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupInviteStatus;

public record CreateGroupInviteResult(
        Long groupId,
        String inviteCode,
        LocalDateTime expiresAt,
        GroupInviteStatus status
) {
}
