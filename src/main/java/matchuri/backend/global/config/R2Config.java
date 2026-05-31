package matchuri.backend.global.config;

import java.net.URI;
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
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String publicUrl;

    @Bean
    public S3Client r2Client() {

        return S3Client.builder().
                endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials
                                .create(accessKey,secretKey)))
                .region(Region.US_EAST_1).build();}
}
