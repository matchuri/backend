package matchuri.backend.domain.auth.service;

public record GoogleOAuth2LoginResult(
        Long memberId,
        String refreshToken,
        String exchangeCode
) {
}
