package matchuri.backend.domain.auth.support.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import matchuri.backend.domain.auth.entity.EmailVerification;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.auth.repository.EmailVerificationRepository;
import matchuri.backend.global.exception.AuthenticationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailVerificationTokenVerifierTest {

    @Mock
    private EmailVerificationRepository repository;

    @Mock
    private EmailVerificationTokenGenerator tokenGenerator;

    @Test
    @DisplayName("SIGNUP token이 유효하면 사용 완료 시각을 기록한다")
    void verifySignupTokenMarksTokenUsed() {
        EmailVerification verification = verifiedSignupVerification("tester@example.com", "hashed-token");
        EmailVerificationTokenVerifier verifier = new EmailVerificationTokenVerifier(repository, tokenGenerator);

        when(tokenGenerator.hashToken("ev_raw-token")).thenReturn("hashed-token");
        when(repository.findByVerificationTokenHash("hashed-token")).thenReturn(Optional.of(verification));

        EmailVerification result = verifier.verifySignupToken(" TESTER@example.com ", "ev_raw-token");

        assertThat(result).isSameAs(verification);
        assertThat(verification.getVerificationTokenUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 사용된 SIGNUP token은 인증 실패로 거절한다")
    void verifySignupTokenRejectsUsedToken() {
        EmailVerification verification = verifiedSignupVerification("tester@example.com", "hashed-token");
        verification.markVerificationTokenUsed(LocalDateTime.now());
        EmailVerificationTokenVerifier verifier = new EmailVerificationTokenVerifier(repository, tokenGenerator);

        when(tokenGenerator.hashToken("ev_raw-token")).thenReturn("hashed-token");
        when(repository.findByVerificationTokenHash("hashed-token")).thenReturn(Optional.of(verification));

        assertThatThrownBy(() -> verifier.verifySignupToken("tester@example.com", "ev_raw-token"))
                .isInstanceOf(AuthenticationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.EMAIL_VERIFICATION_FAILED);
    }

    private EmailVerification verifiedSignupVerification(String email, String tokenHash) {
        EmailVerification verification = EmailVerification.issue(
                email,
                null,
                EmailVerificationPurpose.SIGNUP,
                "hashed-code",
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now()
        );
        verification.verify(tokenHash, LocalDateTime.now().plusMinutes(10), LocalDateTime.now());
        return verification;
    }
}
