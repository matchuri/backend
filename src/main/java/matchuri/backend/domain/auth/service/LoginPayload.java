package matchuri.backend.domain.auth.service;

public record LoginPayload(
        String accessToken,
        long expiresIn,
        Long memberId,
        String role
) {
}
