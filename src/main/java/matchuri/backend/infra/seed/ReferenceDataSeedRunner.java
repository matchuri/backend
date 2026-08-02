package matchuri.backend.infra.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Profile("!test")
@ConditionalOnProperty(
        prefix = "matchuri.seed.reference",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class ReferenceDataSeedRunner implements ApplicationRunner {

    private final ReferenceDataSeedService referenceDataSeedService;

    @Override
    public void run(ApplicationArguments args) {
        referenceDataSeedService.initialize();
    }
}
