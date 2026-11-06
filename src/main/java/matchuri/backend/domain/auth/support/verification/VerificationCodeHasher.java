package matchuri.backend.domain.auth.support.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerificationCodeHasher {

    private final PasswordEncoder passwordEncoder;

    public String hash(String code) {
        return passwordEncoder.encode(code);
    }

    public boolean matches(String code, String codeHash) {
        return passwordEncoder.matches(code, codeHash);
    }
}
