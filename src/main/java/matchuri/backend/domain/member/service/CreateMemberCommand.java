package matchuri.backend.domain.member.service;

public record CreateMemberCommand(
        String loginId,
        String password
) {
}
