package matchuri.backend.domain.group.command;

import matchuri.backend.domain.group.entity.GroupInviteResponseType;

public record RespondGroupInviteCommand(
        Long inviteId,
        GroupInviteResponseType responseType
) {
}
