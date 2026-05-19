package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupInviteStatus;

public record CreateNicknameGroupInviteResult(
        Long inviteId,
        Long groupId,
        String groupName,
        Long targetMemberId,
        String targetNickname,
        LocalDateTime expiresAt,
        GroupInviteStatus status
) {
}
