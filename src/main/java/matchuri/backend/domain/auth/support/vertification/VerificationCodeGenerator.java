package matchuri.backend.domain.auth.support.vertification;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class VerificationCodeGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateCode() {
        int code = secureRandom.nextInt(1_000_000);
        return String.format("%06d", code);
    }
}
