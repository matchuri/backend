package matchuri.backend.domain.group.command;

import matchuri.backend.domain.group.entity.GroupInviteStatus;

public record GetMyGroupInvitesCommand(
        GroupInviteStatus status,
        int page,
        int size
) {
}
