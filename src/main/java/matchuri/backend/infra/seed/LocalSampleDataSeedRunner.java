package matchuri.backend.infra.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@Profile("local & !test")
@RequiredArgsConstructor
public class LocalSampleDataSeedRunner implements ApplicationRunner {

    private final LocalSampleDataSeedService localSampleDataSeedService;

    @Override
    public void run(ApplicationArguments args) {
        localSampleDataSeedService.initialize();
    }
}
