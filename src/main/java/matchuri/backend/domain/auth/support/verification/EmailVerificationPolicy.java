package matchuri.backend.domain.auth.support.verification;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.config.MatchuriProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailVerificationPolicy {

    private final MatchuriProperties matchuriProperties;

    public long codeTtlSeconds() {
        return matchuriProperties.getAuth().getEmailVerification().getCodeTtlSeconds();
    }

    public long resendCooldownSeconds() {
        return matchuriProperties.getAuth().getEmailVerification().getResendCooldownSeconds();
    }

    public long tokenTtlSeconds() {
        return matchuriProperties.getAuth().getEmailVerification().getTokenTtlSeconds();
    }

    public int maxAttempts() {
        return matchuriProperties.getAuth().getEmailVerification().getMaxAttempts();
    }

    public LocalDateTime codeExpiresAt(LocalDateTime now) {
        return now.plusSeconds(codeTtlSeconds());
    }

    public LocalDateTime tokenExpiresAt(LocalDateTime now) {
        return now.plusSeconds(tokenTtlSeconds());
    }

    public boolean isInResendCooldown(LocalDateTime lastSentAt, LocalDateTime now) {
        return secondsUntilResend(lastSentAt, now) > 0;
    }

    public long secondsUntilResend(LocalDateTime lastSentAt, LocalDateTime now) {
        long elapsedSeconds = Duration.between(lastSentAt, now).toSeconds();
        return Math.max(0, resendCooldownSeconds() - elapsedSeconds);
    }
}
