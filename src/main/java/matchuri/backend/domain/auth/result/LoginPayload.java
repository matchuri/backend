package matchuri.backend.domain.auth.result;

public record LoginPayload(
        String accessToken,
        long expiresIn,
        Long memberId,
        String role
) {
}
