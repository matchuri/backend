package matchuri.backend.domain.auth.command;

import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;

public record SendEmailVerificationCommand(
        String email,
        EmailVerificationPurpose purpose,
        String loginId
) {
    public SendEmailVerificationCommand {
        email = email == null ? null : email.trim().toLowerCase();
        loginId = loginId == null || loginId.isBlank() ? null : loginId.trim();
    }
}
