package matchuri.backend.domain.group.command;

import matchuri.backend.domain.group.entity.GroupRoomStatus;

public record GetMyGroupsCommand(
        GroupRoomStatus status,
        int page,
        int size
) {
}
