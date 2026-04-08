package matchuri.backend.domain.auth.service;

public record OAuth2LoginResult(
        Long memberId,
        String refreshToken,
        String exchangeCode
) {
}
