package matchuri.backend.infra.auth.recaptcha;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

record ReCaptchaVerificationResponse(
        boolean success,
        Double score,
        String action,
        @JsonProperty("error-codes") List<String> errorCodes
) {
    boolean hasSecretConfigurationError() {
        return errorCodes != null && (errorCodes.contains("missing-input-secret")
                || errorCodes.contains("invalid-input-secret"));
    }
}
