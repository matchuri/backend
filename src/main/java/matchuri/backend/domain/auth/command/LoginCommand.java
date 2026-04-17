package matchuri.backend.domain.auth.command;

public record LoginCommand(
        String loginId,
        String password
) {
}
