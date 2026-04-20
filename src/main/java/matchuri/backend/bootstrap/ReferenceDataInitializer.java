package matchuri.backend.bootstrap;

import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.global.config.MatchuriProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile({"local", "dev"})
public class ReferenceDataInitializer {

    private final MatchuriProperties matchuriProperties;
    private final AttributeCategoryRepository attributeCategoryRepository;
    private final IngredientRepository ingredientRepository;

    public ReferenceDataInitializer(
            MatchuriProperties matchuriProperties,
            AttributeCategoryRepository attributeCategoryRepository,
            IngredientRepository ingredientRepository
    ) {
        this.matchuriProperties = matchuriProperties;
        this.attributeCategoryRepository = attributeCategoryRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional
    public int initialize() {
        if (!matchuriProperties.getSeed().isEnabled()) {
            log.info("Reference seed initialization skipped because matchuri.seed.enabled=false");
            return 0;
        }

        int createdCount = 0;
        createdCount += createAttributeCategoryIfAbsent(CategoryType.FLAVOR, "SPICY", "매운맛", 10);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.COOKING_METHOD, "SOUP", "국물", 10);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.FOOD_CATEGORY, "RICE", "밥", 10);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.TEXTURE, "CRISPY", "바삭함", 10);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.TEMPERATURE, "HOT", "뜨거움", 10);

        createdCount += createIngredientIfAbsent("PEANUT", "땅콩", true, 10);
        createdCount += createIngredientIfAbsent("SHRIMP", "새우", true, 20);
        createdCount += createIngredientIfAbsent("MILK", "우유", true, 30);
        createdCount += createIngredientIfAbsent("EGG", "계란", true, 40);

        log.info("Reference seed initialization completed. createdCount={}", createdCount);
        return createdCount;
    }

    private int createAttributeCategoryIfAbsent(CategoryType categoryType, String code, String name, int sortOrder) {
        if (attributeCategoryRepository.existsByCategoryTypeAndCode(categoryType, code)) {
            log.info("Attribute category already exists. categoryType={}, code={}", categoryType, code);
            return 0;
        }

        attributeCategoryRepository.save(new AttributeCategory(categoryType, code, name, sortOrder));
        log.info("Attribute category created. categoryType={}, code={}", categoryType, code);
        return 1;
    }

    private int createIngredientIfAbsent(String code, String name, boolean allergen, int sortOrder) {
        if (ingredientRepository.existsByCode(code)) {
            log.info("Ingredient already exists. code={}", code);
            return 0;
        }

        ingredientRepository.save(new Ingredient(code, name, allergen, sortOrder));
        log.info("Ingredient created. code={}", code);
        return 1;
    }
}
