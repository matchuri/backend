package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupMemberStatus;

public record LeaveGroupResult(
        Long groupId,
        GroupMemberStatus memberStatus,
        LocalDateTime leftAt
) {
}
