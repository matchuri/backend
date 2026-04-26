package matchuri.backend.domain.auth.support.verification;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.entity.EmailVerification;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.domain.auth.entity.EmailVerificationStatus;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.auth.repository.EmailVerificationRepository;
import matchuri.backend.global.exception.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailVerificationTokenVerifier {

    private final EmailVerificationRepository repository;
    private final EmailVerificationTokenGenerator tokenGenerator;

    public EmailVerification verifySignupToken(String email, String token) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        LocalDateTime now = LocalDateTime.now();
        EmailVerification verification = findByToken(token);

        if (!isUsableSignupToken(verification, normalizedEmail, now)) {
            throw new AuthenticationException(AuthErrorCode.EMAIL_VERIFICATION_FAILED);
        }

        verification.markVerificationTokenUsed(now);
        return verification;
    }

    public EmailVerification verifyFindLoginIdToken(String token) {
        LocalDateTime now = LocalDateTime.now();
        EmailVerification verification = findByToken(token);

        if (!isUsableToken(verification, EmailVerificationPurpose.FIND_LOGIN_ID, now)) {
            throw new AuthenticationException(AuthErrorCode.EMAIL_VERIFICATION_FAILED);
        }

        verification.markVerificationTokenUsed(now);
        return verification;
    }

    public EmailVerification verifyResetPasswordToken(String loginId, String token) {
        String normalizedLoginId = loginId == null ? null : loginId.trim();
        LocalDateTime now = LocalDateTime.now();
        EmailVerification verification = findByToken(token);

        if (!isUsableResetPasswordToken(verification, normalizedLoginId, now)) {
            throw new AuthenticationException(AuthErrorCode.EMAIL_VERIFICATION_FAILED);
        }

        verification.markVerificationTokenUsed(now);
        return verification;
    }

    private EmailVerification findByToken(String token) {
        return repository.findByVerificationTokenHash(tokenGenerator.hashToken(token))
                .orElseThrow(() -> new AuthenticationException(AuthErrorCode.EMAIL_VERIFICATION_FAILED));
    }

    private boolean isUsableSignupToken(EmailVerification verification, String email, LocalDateTime now) {
        return isUsableToken(verification, EmailVerificationPurpose.SIGNUP, now)
                && verification.getEmail().equals(email)
                && verification.getLoginId() == null;
    }

    private boolean isUsableResetPasswordToken(EmailVerification verification, String loginId, LocalDateTime now) {
        return isUsableToken(verification, EmailVerificationPurpose.RESET_PASSWORD, now)
                && verification.getLoginId() != null
                && verification.getLoginId().equals(loginId);
    }

    private boolean isUsableToken(
            EmailVerification verification,
            EmailVerificationPurpose purpose,
            LocalDateTime now
    ) {
        return verification.getStatus() == EmailVerificationStatus.VERIFIED
                && verification.getPurpose() == purpose
                && verification.getVerificationTokenHash() != null
                && verification.getVerificationTokenExpiresAt() != null
                && verification.getVerificationTokenExpiresAt().isAfter(now)
                && verification.getVerificationTokenUsedAt() == null;
    }
}
