package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupMemberStatus;

public record GroupMemberSummaryResult(
        Long memberId,
        String nickname,
        GroupMemberRole role,
        GroupMemberStatus status,
        LocalDateTime joinedAt,
        boolean isMe
) {
}
