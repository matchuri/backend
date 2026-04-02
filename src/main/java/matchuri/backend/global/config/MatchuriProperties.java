package matchuri.backend.global.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "matchuri")
public class MatchuriProperties {

    @Valid
    @NotNull
    private Auth auth;

    @Valid
    @NotNull
    private Seed seed;

    @Getter
    @Setter
    public static class Auth {
        @NotNull
        private List<String> publicApiPatterns;

        @NotNull
        private List<String> publicGetApiPatterns;

        @NotNull
        private List<String> publicPostApiPatterns;

        @NotNull
        private List<String> publicOptionsApiPatterns;

        @Valid
        @NotNull
        private Cors cors;

        @Valid
        @NotNull
        private Jwt jwt;
    }

    @Getter
    @Setter
    public static class Cors {
        @NotNull
        private List<String> allowedOrigins;

        @NotNull
        private List<String> allowedMethods;

        @NotNull
        private List<String> allowedHeaders;

        @NotNull
        private List<String> exposedHeaders;

        private boolean allowCredentials;

        @Positive
        private long maxAge;
    }

    @Getter
    @Setter
    public static class Jwt {
        @NotBlank
        private String secret;

        @NotBlank
        private String issuer;

        @Positive
        private long accessTokenExpirationSeconds;

        @Positive
        private long refreshTokenExpirationSeconds;
    }

    @Getter
    @Setter
    public static class Seed {
        private boolean enabled;
        private boolean sampleMembersEnabled;
    }
}
