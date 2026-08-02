package matchuri.backend.infra.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@Profile("local & !test")
@ConditionalOnProperty(
        prefix = "matchuri.seed.sample",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class LocalSampleDataSeedRunner implements ApplicationRunner {

    private final LocalSampleDataSeedService localSampleDataSeedService;

    @Override
    public void run(ApplicationArguments args) {
        localSampleDataSeedService.initialize();
    }
}
