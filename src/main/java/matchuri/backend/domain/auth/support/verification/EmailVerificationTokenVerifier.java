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
        EmailVerification verification = repository.findByVerificationTokenHash(tokenGenerator.hashToken(token))
                .orElseThrow(() -> new AuthenticationException(AuthErrorCode.EMAIL_VERIFICATION_FAILED));

        if (!isUsableSignupToken(verification, normalizedEmail, now)) {
            throw new AuthenticationException(AuthErrorCode.EMAIL_VERIFICATION_FAILED);
        }

        verification.markVerificationTokenUsed(now);
        return verification;
    }

    private boolean isUsableSignupToken(EmailVerification verification, String email, LocalDateTime now) {
        return verification.getStatus() == EmailVerificationStatus.VERIFIED
                && verification.getPurpose() == EmailVerificationPurpose.SIGNUP
                && verification.getEmail().equals(email)
                && verification.getLoginId() == null
                && verification.getVerificationTokenHash() != null
                && verification.getVerificationTokenExpiresAt() != null
                && verification.getVerificationTokenExpiresAt().isAfter(now)
                && verification.getVerificationTokenUsedAt() == null;
    }
}
