package matchuri.backend.infra.auth.captcha.google;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "captcha.google")
public class GoogleRecaptchaProperties {

    @NotBlank
    private String secretKey;

    @NotBlank
    private String verifyUrl;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double scoreThreshold;

    @Positive
    private int connectTimeoutMillis;

    @Positive
    private int readTimeoutMillis;
}
