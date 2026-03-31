package matchuri.backend.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class SeedDataInitializer implements ApplicationRunner {

    private final ReferenceDataInitializer referenceDataInitializer;
    private final DevSampleDataInitializer devSampleDataInitializer;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        int referenceSeedCount = referenceDataInitializer.initialize();
        int sampleSeedCount = devSampleDataInitializer.initialize();

        log.info(
                "Seed data initialization completed. referenceDataCount={}, sampleDataCount={}",
                referenceSeedCount,
                sampleSeedCount
        );
    }
}
