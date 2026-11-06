package matchuri.backend.global.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.config.MatchuriProperties;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class MatchuriHealthIndicator implements HealthIndicator {

    private static final int DB_VALIDATION_TIMEOUT_SECONDS = 2;

    private final DataSource dataSource;
    private final MatchuriProperties matchuriProperties;

    @Override
    public Health health() {
        boolean databaseReachable = isDatabaseReachable();
        boolean authenticationReady = isAuthenticationReady();

        Health.Builder builder = databaseReachable && authenticationReady
                ? Health.up()
                : Health.down();

        return builder
                .withDetail("service", "matchuri-backend")
                .withDetail("checkedAt", Instant.now())
                .withDetail("databaseReachable", databaseReachable)
                .withDetail("authenticationReady", authenticationReady)
                .build();
    }

    private boolean isDatabaseReachable() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(DB_VALIDATION_TIMEOUT_SECONDS);
        } catch (SQLException ex) {
            return false;
        }
    }

    private boolean isAuthenticationReady() {
        MatchuriProperties.Auth auth = matchuriProperties.getAuth();
        MatchuriProperties.OAuth2 oauth2 = auth.getOauth2();
        MatchuriProperties.Jwt jwt = auth.getJwt();

        return StringUtils.hasText(oauth2.getFrontendBaseUrl())
                && StringUtils.hasText(oauth2.getSuccessPath())
                && StringUtils.hasText(oauth2.getFailurePath())
                && StringUtils.hasText(jwt.getSecret())
                && StringUtils.hasText(jwt.getIssuer());
    }
}
