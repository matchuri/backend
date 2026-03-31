package matchuri.backend.bootstrap;

import lombok.extern.slf4j.Slf4j;
import matchuri.backend.global.config.MatchuriProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"local", "dev"})
public class ReferenceDataInitializer {

    private final MatchuriProperties matchuriProperties;

    public ReferenceDataInitializer(MatchuriProperties matchuriProperties) {
        this.matchuriProperties = matchuriProperties;
    }

    public int initialize() {
        if (!matchuriProperties.getSeed().isEnabled()) {
            log.info("Reference seed initialization skipped because matchuri.seed.enabled=false");
            return 0;
        }

        log.info("Reference seed initialization is reserved for attribute_categories/ingredients/menu_items in later steps.");
        return 0;
    }
}
