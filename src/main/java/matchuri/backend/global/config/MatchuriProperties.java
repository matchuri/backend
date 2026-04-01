package matchuri.backend.global.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "matchuri")
public class MatchuriProperties {

    private Auth auth = new Auth();
    private Seed seed = new Seed();

    @Getter
    @Setter
    public static class Auth {
        private List<String> publicApiPatterns = List.of(
                "/api/v1/members/exists/**",
                "/api/v1/members",
                "/api/v1/auth/**",
                "/docs/**",
                "/error"
        );
        private Jwt jwt = new Jwt();
    }

    @Getter
    @Setter
    public static class Jwt {
        private String secret = "matchuri-local-jwt-secret-key-matchuri-local-jwt-secret-key";
        private String issuer = "matchuri-backend";
        private long accessTokenExpirationSeconds = 3600;
        private long refreshTokenExpirationSeconds = 1_209_600;
    }

    @Getter
    @Setter
    public static class Seed {
        private boolean enabled = false;
        private boolean sampleMembersEnabled = true;
    }
}
