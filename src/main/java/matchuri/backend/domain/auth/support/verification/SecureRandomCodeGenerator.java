package matchuri.backend.domain.auth.support.verification;

import java.security.SecureRandom;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class SecureRandomCodeGenerator implements VerificationCodeGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateCode() {
        int code = secureRandom.nextInt(1_000_000);
        return String.format("%06d", code);
    }

}
