package matchuri.backend.domain.auth.service;

public record IssuedAccessToken(
        String accessToken,
        long expiresIn
) {
}
