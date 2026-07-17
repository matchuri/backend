package matchuri.backend.global.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "recaptcha")
public class ReCaptchaConfig {
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

    @Bean("recaptchaRestClient")
    public RestClient recaptchaRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMillis));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMillis));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
