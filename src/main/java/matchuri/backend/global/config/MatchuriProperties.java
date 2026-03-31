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

    @Getter
    @Setter
    public static class Auth {
        private List<String> publicApiPatterns = List.of(
                "/api/v1/auth/**",
                "/error"
        );
    }
}
