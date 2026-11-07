package matchuri.backend.infra.auth.captcha.google;

import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "captcha", name = "provider", havingValue = "google", matchIfMissing = true)
@EnableConfigurationProperties(GoogleRecaptchaProperties.class)
public class GoogleRecaptchaConfig {

    @Bean("googleCaptchaRestClient")
    public RestClient googleCaptchaRestClient(GoogleRecaptchaProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMillis()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMillis()));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
