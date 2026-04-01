package matchuri.backend.api.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        LoginMemberSummary member
) {

    public record LoginMemberSummary(
            Long id,
            String role
    ) {
    }
}
