package matchuri.backend.domain.auth.result;

public record LoginResult(
        LoginPayload payload,
        String refreshToken
) {
}
