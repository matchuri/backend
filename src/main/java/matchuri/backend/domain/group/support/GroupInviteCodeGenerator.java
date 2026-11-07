package matchuri.backend.domain.group.support;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class GroupInviteCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);

        for (int index = 0; index < CODE_LENGTH; index++) {
            code.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }

        return code.toString();
    }
}
