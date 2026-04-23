package matchuri.backend.bootstrap;

import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
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
    private final MenuItemRepository menuItemRepository;

    public ReferenceDataInitializer(
            MatchuriProperties matchuriProperties,
            AttributeCategoryRepository attributeCategoryRepository,
            IngredientRepository ingredientRepository,
            MenuItemRepository menuItemRepository
    ) {
        this.matchuriProperties = matchuriProperties;
        this.attributeCategoryRepository = attributeCategoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.menuItemRepository = menuItemRepository;
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

        createdCount += createMenuItemIfAbsent("KIMCHI_STEW", "김치찌개", "김치와 돼지고기를 넣고 끓인 대표적인 한식 찌개입니다.");
        createdCount += createMenuItemIfAbsent("DOENJANG_STEW", "된장찌개", "된장을 기본으로 두부와 채소를 넣어 끓인 구수한 찌개입니다.");
        createdCount += createMenuItemIfAbsent("BIBIMBAP", "비빔밥", "밥 위에 여러 나물과 고추장을 올려 비벼 먹는 한식 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("PORK_CUTLET", "돈까스", "돼지고기를 튀겨 소스와 함께 먹는 바삭한 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("JJAJANGMYEON", "짜장면", "춘장 소스에 면을 비벼 먹는 중식 면 요리입니다.");
        createdCount += createMenuItemIfAbsent("JJAMPPONG", "짬뽕", "해산물과 채소를 넣은 매콤한 국물 면 요리입니다.");
        createdCount += createMenuItemIfAbsent("RAMEN", "라멘", "진한 육수와 면을 함께 즐기는 일본식 면 요리입니다.");
        createdCount += createMenuItemIfAbsent("SUSHI", "초밥", "초밥용 밥 위에 생선이나 재료를 올린 메뉴입니다.");

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

    private int createMenuItemIfAbsent(String code, String name, String description) {
        if (menuItemRepository.existsByCode(code)) {
            log.info("Menu item already exists. code={}", code);
            return 0;
        }

        menuItemRepository.save(new MenuItem(code, name, description));
        log.info("Menu item created. code={}", code);
        return 1;
    }
}
