package matchuri.backend.global.config;

import java.net.URI;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "cloudflare.r2")
public class R2Config {
    @NotBlank
    private String endpoint;

    @NotBlank
    private String accessKey;

    @NotBlank
    private String secretKey;

    @NotBlank
    private String bucket;

    @NotBlank
    private String publicUrl;

    @NotBlank
    private String region;

    @Positive
    private long maxUploadBytes;

    @Positive
    private int minImageWidth;

    @Positive
    private int minImageHeight;

    @Positive
    private int maxImageWidth;

    @Positive
    private int maxImageHeight;

    @NotBlank
    private String cacheControl;

    @Bean
    public S3Client r2Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials
                                .create(accessKey, secretKey)))
                .region(Region.of(region))
                .build();
    }
}
