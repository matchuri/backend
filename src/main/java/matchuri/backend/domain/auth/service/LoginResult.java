package matchuri.backend.domain.auth.service;

public record LoginResult(
        LoginPayload payload,
        String refreshToken
) {
}
