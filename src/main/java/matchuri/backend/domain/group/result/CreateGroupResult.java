package matchuri.backend.domain.group.result;

import matchuri.backend.domain.group.entity.GroupRoomStatus;

public record CreateGroupResult(
        Long groupId,
        String inviteCode,
        GroupRoomStatus status
) {
}
