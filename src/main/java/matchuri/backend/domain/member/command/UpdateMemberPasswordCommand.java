package matchuri.backend.domain.member.command;

public record UpdateMemberPasswordCommand(
        String currentPassword,
        String newPassword
) {
}
