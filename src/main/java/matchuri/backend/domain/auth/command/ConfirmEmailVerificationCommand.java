package matchuri.backend.domain.auth.command;

import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;

public record ConfirmEmailVerificationCommand(
        String email,
        EmailVerificationPurpose purpose,
        String loginId,
        String code
) {
    public ConfirmEmailVerificationCommand {
        email = email == null ? null : email.trim().toLowerCase();
        loginId = purpose == EmailVerificationPurpose.RESET_PASSWORD && loginId != null && !loginId.isBlank()
                ? loginId.trim()
                : null;
        code = code == null ? null : code.trim();
    }
}
