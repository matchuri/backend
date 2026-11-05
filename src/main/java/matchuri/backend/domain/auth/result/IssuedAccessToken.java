package matchuri.backend.domain.auth.result;

public record IssuedAccessToken(
        String accessToken,
        long expiresIn
) {
}
