package matchuri.backend.domain.auth.result;

public record SendEmailVerificationResult(
        boolean accepted,
        long resendAvailableAfterSeconds
) {
    public static SendEmailVerificationResult accepted(long resendAvailableAfterSeconds) {
        return new SendEmailVerificationResult(true, resendAvailableAfterSeconds);
    }
}
