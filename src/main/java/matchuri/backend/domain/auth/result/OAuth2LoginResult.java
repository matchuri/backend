package matchuri.backend.domain.auth.result;

public record OAuth2LoginResult(
        Long memberId,
        String refreshToken,
        String exchangeCode
) {
}
