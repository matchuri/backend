package matchuri.backend.domain.auth.command;

public record ResetPasswordCommand(
        String loginId,
        String emailVerificationToken,
        String newPassword
) {
    public ResetPasswordCommand {
        loginId = loginId == null ? null : loginId.trim();
        emailVerificationToken = emailVerificationToken == null ? null : emailVerificationToken.trim();
    }
}
