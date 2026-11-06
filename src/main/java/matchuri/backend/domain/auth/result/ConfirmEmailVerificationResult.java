package matchuri.backend.domain.auth.result;

public record ConfirmEmailVerificationResult(
        boolean verified,
        String emailVerificationToken,
        long expiresIn
) {
    public static ConfirmEmailVerificationResult verified(String emailVerificationToken, long expiresIn) {
        return new ConfirmEmailVerificationResult(true, emailVerificationToken, expiresIn);
    }
}
