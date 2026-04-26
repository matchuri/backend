package matchuri.backend.api.auth.dto.request;

public record EmailVerificationRequest(
        String email,
        String subject,
        String content
) {
}
