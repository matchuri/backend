package matchuri.backend.domain.group.command;

public record CreateNicknameGroupInviteCommand(
        Long groupId,
        String nickname
) {
}
