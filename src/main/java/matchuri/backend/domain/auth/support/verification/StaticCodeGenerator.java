package matchuri.backend.domain.auth.support.verification;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
public class StaticCodeGenerator implements VerificationCodeGenerator {

    @Override
    public String generateCode() {
        return "123456";
    }

}
