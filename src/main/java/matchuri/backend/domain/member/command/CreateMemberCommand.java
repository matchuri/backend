package matchuri.backend.domain.member.command;

public record CreateMemberCommand(
        String loginId,
        String password
) {
}
