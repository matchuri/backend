package matchuri.backend.domain.auth.service;

public record LoginCommand(
        String loginId,
        String password
) {
}
