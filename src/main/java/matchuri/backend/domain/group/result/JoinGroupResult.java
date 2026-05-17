package matchuri.backend.domain.group.result;

import matchuri.backend.domain.group.entity.GroupMemberStatus;

public record JoinGroupResult(
        Long groupId,
        GroupMemberStatus memberStatus
) {
}
