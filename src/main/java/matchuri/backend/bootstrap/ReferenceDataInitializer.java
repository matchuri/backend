package matchuri.backend.bootstrap;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.entity.MenuAttributeCategory;
import matchuri.backend.domain.menu.entity.MenuIngredient;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.repository.MenuAttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.MenuIngredientRepository;
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
    private final MenuAttributeCategoryRepository menuAttributeCategoryRepository;
    private final MenuIngredientRepository menuIngredientRepository;

    public ReferenceDataInitializer(
            MatchuriProperties matchuriProperties,
            AttributeCategoryRepository attributeCategoryRepository,
            IngredientRepository ingredientRepository,
            MenuItemRepository menuItemRepository,
            MenuAttributeCategoryRepository menuAttributeCategoryRepository,
            MenuIngredientRepository menuIngredientRepository
    ) {
        this.matchuriProperties = matchuriProperties;
        this.attributeCategoryRepository = attributeCategoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.menuItemRepository = menuItemRepository;
        this.menuAttributeCategoryRepository = menuAttributeCategoryRepository;
        this.menuIngredientRepository = menuIngredientRepository;
    }

    @Transactional
    public int initialize() {
        if (!matchuriProperties.getSeed().isEnabled()) {
            log.info("Reference seed initialization skipped because matchuri.seed.enabled=false");
            return 0;
        }

        int createdCount = 0;
        createdCount += createAttributeCategories();
        createdCount += createIngredients();
        createdCount += createMenuItems();
        createdCount += createMenuAttributeMappings();
        createdCount += createMenuIngredientMappings();

        log.info("Reference seed initialization completed. createdCount={}", createdCount);
        return createdCount;
    }

    private int createAttributeCategories() {
        int createdCount = 0;

        createdCount += createAttributeCategoryIfAbsent(CategoryType.FLAVOR, "SPICY", "매콤", 10);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.FLAVOR, "SWEET", "달콤", 20);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.FLAVOR, "SALTY", "짭짤", 30);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.FLAVOR, "NUTTY", "고소", 40);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.FLAVOR, "FRESH", "상큼", 50);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.FLAVOR, "RICH", "진한/묵직한", 60);

        createdCount += createAttributeCategoryIfAbsent(CategoryType.COOKING_METHOD, "SOUP", "국물/탕", 10);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.COOKING_METHOD, "GRILLED", "구이", 20);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.COOKING_METHOD, "FRIED", "튀김", 30);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.COOKING_METHOD, "STIR_FRIED", "볶음", 40);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.COOKING_METHOD, "STEAMED", "찜", 50);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.COOKING_METHOD, "RAW_SALAD", "생식/샐러드", 60);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.COOKING_METHOD, "NOODLE_MIXED", "면/비빔", 70);

        createdCount += createAttributeCategoryIfAbsent(CategoryType.FOOD_CATEGORY, "KOREAN", "한식", 10);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.FOOD_CATEGORY, "CHINESE", "중식", 20);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.FOOD_CATEGORY, "JAPANESE", "일식", 30);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.FOOD_CATEGORY, "WESTERN", "양식", 40);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.FOOD_CATEGORY, "BUNSIK", "분식", 50);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.FOOD_CATEGORY, "FAST_FOOD", "패스트푸드", 60);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.FOOD_CATEGORY, "ASIAN", "아시안", 70);

        createdCount += createAttributeCategoryIfAbsent(CategoryType.TEXTURE, "CRISPY", "바삭", 10);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.TEXTURE, "CHEWY", "쫄깃", 20);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.TEXTURE, "CRUNCHY", "아삭", 30);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.TEXTURE, "SOFT", "부드러움", 40);

        createdCount += createAttributeCategoryIfAbsent(CategoryType.TEMPERATURE, "HOT", "뜨거움", 10);
        createdCount += createAttributeCategoryIfAbsent(CategoryType.TEMPERATURE, "COLD", "차가움", 20);

        return createdCount;
    }

    private int createIngredients() {
        int createdCount = 0;

        createdCount += createIngredientIfAbsent("PEANUT", "땅콩", true, 10);
        createdCount += createIngredientIfAbsent("SHRIMP", "새우", true, 20);
        createdCount += createIngredientIfAbsent("MILK", "우유", true, 30);
        createdCount += createIngredientIfAbsent("EGG", "계란", true, 40);
        createdCount += createIngredientIfAbsent("WHEAT", "밀", true, 50);
        createdCount += createIngredientIfAbsent("SOYBEAN", "대두", true, 60);
        createdCount += createIngredientIfAbsent("BUCKWHEAT", "메밀", true, 70);
        createdCount += createIngredientIfAbsent("CRAB", "게", true, 80);
        createdCount += createIngredientIfAbsent("SQUID", "오징어", true, 90);
        createdCount += createIngredientIfAbsent("SHELLFISH", "조개류", true, 100);
        createdCount += createIngredientIfAbsent("MACKEREL", "고등어", true, 110);
        createdCount += createIngredientIfAbsent("PORK", "돼지고기", false, 120);
        createdCount += createIngredientIfAbsent("BEEF", "소고기", false, 130);
        createdCount += createIngredientIfAbsent("CHICKEN", "닭고기", false, 140);
        createdCount += createIngredientIfAbsent("FISH", "생선", false, 150);
        createdCount += createIngredientIfAbsent("TOFU", "두부", false, 160);
        createdCount += createIngredientIfAbsent("KIMCHI", "김치", false, 170);
        createdCount += createIngredientIfAbsent("CHEESE", "치즈", false, 180);
        createdCount += createIngredientIfAbsent("GARLIC", "마늘", false, 190);
        createdCount += createIngredientIfAbsent("CILANTRO", "고수", false, 200);
        createdCount += createIngredientIfAbsent("MUSHROOM", "버섯", false, 210);
        createdCount += createIngredientIfAbsent("SESAME", "참깨", true, 220);

        return createdCount;
    }

    private int createMenuItems() {
        int createdCount = 0;

        createdCount += createMenuItemIfAbsent("KIMCHI_STEW", "김치찌개", "김치와 돼지고기를 넣고 끓인 대표적인 한식 찌개입니다.");
        createdCount += createMenuItemIfAbsent("DOENJANG_STEW", "된장찌개", "된장을 기본으로 두부와 채소를 넣어 끓인 구수한 찌개입니다.");
        createdCount += createMenuItemIfAbsent("BIBIMBAP", "비빔밥", "밥 위에 여러 나물과 고추장을 올려 비벼 먹는 한식 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("PORK_CUTLET", "돈까스", "돼지고기를 튀겨 소스와 함께 먹는 바삭한 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("JJAJANGMYEON", "짜장면", "춘장 소스에 면을 비벼 먹는 중식 면 요리입니다.");
        createdCount += createMenuItemIfAbsent("JJAMPPONG", "짬뽕", "해산물과 채소를 넣은 매콤한 국물 면 요리입니다.");
        createdCount += createMenuItemIfAbsent("RAMEN", "라멘", "진한 육수와 면을 함께 즐기는 일본식 면 요리입니다.");
        createdCount += createMenuItemIfAbsent("SUSHI", "초밥", "초밥용 밥 위에 생선이나 재료를 올린 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("BULGOGI", "불고기", "얇게 썬 소고기를 달콤짭짤한 양념에 볶거나 구운 한식 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("SAMGYEOPSAL", "삼겹살", "돼지고기를 구워 쌈과 곁들여 먹는 구이 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("DAK_GALBI", "닭갈비", "닭고기와 채소를 매콤한 양념에 볶아 먹는 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("BUDAE_JJIGAE", "부대찌개", "햄, 소시지, 김치, 라면 사리를 넣어 끓이는 진한 찌개입니다.");
        createdCount += createMenuItemIfAbsent("GALBITANG", "갈비탕", "소갈비를 오래 끓여 맑고 깊은 국물로 즐기는 한식 탕입니다.");
        createdCount += createMenuItemIfAbsent("NAENGMYEON", "냉면", "차가운 육수나 매콤한 양념으로 먹는 시원한 면 요리입니다.");
        createdCount += createMenuItemIfAbsent("GIMBAP", "김밥", "밥과 여러 재료를 김으로 말아 먹는 간편한 분식 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("TTEOKBOKKI", "떡볶이", "떡과 어묵을 매콤달콤한 양념에 끓인 분식 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("UDON", "우동", "굵은 면과 따뜻한 국물을 함께 먹는 일식 면 요리입니다.");
        createdCount += createMenuItemIfAbsent("SOBA", "소바", "메밀면을 차갑거나 따뜻한 국물에 곁들이는 일식 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("GYUDON", "규동", "밥 위에 달콤짭짤하게 조린 소고기를 올린 일본식 덮밥입니다.");
        createdCount += createMenuItemIfAbsent("KARAAGE", "가라아게", "닭고기를 바삭하게 튀긴 일본식 튀김 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("MAPO_TOFU", "마파두부", "두부와 다진 고기를 매콤한 소스에 볶은 중식 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("TANGSUYUK", "탕수육", "튀긴 고기에 새콤달콤한 소스를 곁들이는 중식 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("FRIED_RICE", "볶음밥", "밥과 채소, 고기 또는 해산물을 함께 볶은 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("MALA_XIANG_GUO", "마라샹궈", "다양한 재료를 마라 양념에 볶아 먹는 중식 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("PASTA", "파스타", "면과 소스를 중심으로 즐기는 대표적인 양식 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("PIZZA", "피자", "도우 위에 토핑과 치즈를 올려 구운 양식 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("HAMBURGER", "햄버거", "빵 사이에 패티와 채소, 소스를 넣은 패스트푸드 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("SALAD", "샐러드", "신선한 채소와 드레싱을 중심으로 가볍게 먹는 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("STEAK", "스테이크", "고기를 두툼하게 구워 소스와 곁들이는 양식 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("PHO", "쌀국수", "쌀면과 향신료가 들어간 국물을 함께 먹는 베트남식 면 요리입니다.");
        createdCount += createMenuItemIfAbsent("PAD_THAI", "팟타이", "쌀국수와 새우, 채소를 새콤달콤하게 볶은 태국식 면 요리입니다.");
        createdCount += createMenuItemIfAbsent("CURRY_RICE", "카레라이스", "밥에 진한 카레 소스를 얹어 먹는 메뉴입니다.");
        createdCount += createMenuItemIfAbsent("BANH_MI", "반미", "바게트에 고기와 채소를 넣어 먹는 베트남식 샌드위치입니다.");

        return createdCount;
    }

    private int createMenuAttributeMappings() {
        int createdCount = 0;

        createdCount += mapAttributes("KIMCHI_STEW", List.of(
                category(CategoryType.FLAVOR, "SPICY"),
                category(CategoryType.FLAVOR, "RICH"),
                category(CategoryType.COOKING_METHOD, "SOUP"),
                category(CategoryType.FOOD_CATEGORY, "KOREAN"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("DOENJANG_STEW", List.of(
                category(CategoryType.FLAVOR, "RICH"),
                category(CategoryType.COOKING_METHOD, "SOUP"),
                category(CategoryType.FOOD_CATEGORY, "KOREAN"),
                category(CategoryType.TEXTURE, "SOFT"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("BIBIMBAP", List.of(
                category(CategoryType.FLAVOR, "NUTTY"),
                category(CategoryType.COOKING_METHOD, "NOODLE_MIXED"),
                category(CategoryType.FOOD_CATEGORY, "KOREAN"),
                category(CategoryType.TEXTURE, "CRUNCHY"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("PORK_CUTLET", List.of(
                category(CategoryType.FLAVOR, "SALTY"),
                category(CategoryType.COOKING_METHOD, "FRIED"),
                category(CategoryType.FOOD_CATEGORY, "JAPANESE"),
                category(CategoryType.TEXTURE, "CRISPY"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("JJAJANGMYEON", List.of(
                category(CategoryType.FLAVOR, "SWEET"),
                category(CategoryType.FLAVOR, "RICH"),
                category(CategoryType.COOKING_METHOD, "NOODLE_MIXED"),
                category(CategoryType.FOOD_CATEGORY, "CHINESE"),
                category(CategoryType.TEXTURE, "CHEWY"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("JJAMPPONG", List.of(
                category(CategoryType.FLAVOR, "SPICY"),
                category(CategoryType.COOKING_METHOD, "SOUP"),
                category(CategoryType.FOOD_CATEGORY, "CHINESE"),
                category(CategoryType.TEXTURE, "CHEWY"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("RAMEN", List.of(
                category(CategoryType.FLAVOR, "RICH"),
                category(CategoryType.COOKING_METHOD, "SOUP"),
                category(CategoryType.COOKING_METHOD, "NOODLE_MIXED"),
                category(CategoryType.FOOD_CATEGORY, "JAPANESE"),
                category(CategoryType.TEXTURE, "CHEWY"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("SUSHI", List.of(
                category(CategoryType.FLAVOR, "FRESH"),
                category(CategoryType.COOKING_METHOD, "RAW_SALAD"),
                category(CategoryType.FOOD_CATEGORY, "JAPANESE"),
                category(CategoryType.TEXTURE, "SOFT"),
                category(CategoryType.TEMPERATURE, "COLD")
        ));
        createdCount += mapAttributes("BULGOGI", List.of(
                category(CategoryType.FLAVOR, "SWEET"),
                category(CategoryType.FLAVOR, "SALTY"),
                category(CategoryType.COOKING_METHOD, "GRILLED"),
                category(CategoryType.FOOD_CATEGORY, "KOREAN"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("SAMGYEOPSAL", List.of(
                category(CategoryType.FLAVOR, "SALTY"),
                category(CategoryType.COOKING_METHOD, "GRILLED"),
                category(CategoryType.FOOD_CATEGORY, "KOREAN"),
                category(CategoryType.TEXTURE, "CRISPY"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("DAK_GALBI", List.of(
                category(CategoryType.FLAVOR, "SPICY"),
                category(CategoryType.FLAVOR, "SWEET"),
                category(CategoryType.COOKING_METHOD, "STIR_FRIED"),
                category(CategoryType.FOOD_CATEGORY, "KOREAN"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("BUDAE_JJIGAE", List.of(
                category(CategoryType.FLAVOR, "SPICY"),
                category(CategoryType.FLAVOR, "RICH"),
                category(CategoryType.COOKING_METHOD, "SOUP"),
                category(CategoryType.FOOD_CATEGORY, "KOREAN"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("GALBITANG", List.of(
                category(CategoryType.FLAVOR, "RICH"),
                category(CategoryType.COOKING_METHOD, "SOUP"),
                category(CategoryType.FOOD_CATEGORY, "KOREAN"),
                category(CategoryType.TEXTURE, "SOFT"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("NAENGMYEON", List.of(
                category(CategoryType.FLAVOR, "FRESH"),
                category(CategoryType.COOKING_METHOD, "NOODLE_MIXED"),
                category(CategoryType.FOOD_CATEGORY, "KOREAN"),
                category(CategoryType.TEXTURE, "CHEWY"),
                category(CategoryType.TEMPERATURE, "COLD")
        ));
        createdCount += mapAttributes("GIMBAP", List.of(
                category(CategoryType.FLAVOR, "NUTTY"),
                category(CategoryType.COOKING_METHOD, "NOODLE_MIXED"),
                category(CategoryType.FOOD_CATEGORY, "BUNSIK"),
                category(CategoryType.TEXTURE, "SOFT"),
                category(CategoryType.TEMPERATURE, "COLD")
        ));
        createdCount += mapAttributes("TTEOKBOKKI", List.of(
                category(CategoryType.FLAVOR, "SPICY"),
                category(CategoryType.FLAVOR, "SWEET"),
                category(CategoryType.COOKING_METHOD, "STIR_FRIED"),
                category(CategoryType.FOOD_CATEGORY, "BUNSIK"),
                category(CategoryType.TEXTURE, "CHEWY"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("UDON", List.of(
                category(CategoryType.FLAVOR, "RICH"),
                category(CategoryType.COOKING_METHOD, "SOUP"),
                category(CategoryType.COOKING_METHOD, "NOODLE_MIXED"),
                category(CategoryType.FOOD_CATEGORY, "JAPANESE"),
                category(CategoryType.TEXTURE, "CHEWY"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("SOBA", List.of(
                category(CategoryType.FLAVOR, "FRESH"),
                category(CategoryType.COOKING_METHOD, "NOODLE_MIXED"),
                category(CategoryType.FOOD_CATEGORY, "JAPANESE"),
                category(CategoryType.TEXTURE, "CHEWY"),
                category(CategoryType.TEMPERATURE, "COLD")
        ));
        createdCount += mapAttributes("GYUDON", List.of(
                category(CategoryType.FLAVOR, "SWEET"),
                category(CategoryType.FLAVOR, "SALTY"),
                category(CategoryType.COOKING_METHOD, "STIR_FRIED"),
                category(CategoryType.FOOD_CATEGORY, "JAPANESE"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("KARAAGE", List.of(
                category(CategoryType.FLAVOR, "SALTY"),
                category(CategoryType.COOKING_METHOD, "FRIED"),
                category(CategoryType.FOOD_CATEGORY, "JAPANESE"),
                category(CategoryType.TEXTURE, "CRISPY"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("MAPO_TOFU", List.of(
                category(CategoryType.FLAVOR, "SPICY"),
                category(CategoryType.FLAVOR, "RICH"),
                category(CategoryType.COOKING_METHOD, "STIR_FRIED"),
                category(CategoryType.FOOD_CATEGORY, "CHINESE"),
                category(CategoryType.TEXTURE, "SOFT"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("TANGSUYUK", List.of(
                category(CategoryType.FLAVOR, "SWEET"),
                category(CategoryType.FLAVOR, "FRESH"),
                category(CategoryType.COOKING_METHOD, "FRIED"),
                category(CategoryType.FOOD_CATEGORY, "CHINESE"),
                category(CategoryType.TEXTURE, "CRISPY"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("FRIED_RICE", List.of(
                category(CategoryType.FLAVOR, "SALTY"),
                category(CategoryType.COOKING_METHOD, "STIR_FRIED"),
                category(CategoryType.FOOD_CATEGORY, "CHINESE"),
                category(CategoryType.TEXTURE, "SOFT"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("MALA_XIANG_GUO", List.of(
                category(CategoryType.FLAVOR, "SPICY"),
                category(CategoryType.FLAVOR, "RICH"),
                category(CategoryType.COOKING_METHOD, "STIR_FRIED"),
                category(CategoryType.FOOD_CATEGORY, "CHINESE"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("PASTA", List.of(
                category(CategoryType.FLAVOR, "RICH"),
                category(CategoryType.COOKING_METHOD, "NOODLE_MIXED"),
                category(CategoryType.FOOD_CATEGORY, "WESTERN"),
                category(CategoryType.TEXTURE, "CHEWY"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("PIZZA", List.of(
                category(CategoryType.FLAVOR, "SALTY"),
                category(CategoryType.FLAVOR, "RICH"),
                category(CategoryType.COOKING_METHOD, "GRILLED"),
                category(CategoryType.FOOD_CATEGORY, "WESTERN"),
                category(CategoryType.TEXTURE, "CRISPY"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("HAMBURGER", List.of(
                category(CategoryType.FLAVOR, "SALTY"),
                category(CategoryType.COOKING_METHOD, "GRILLED"),
                category(CategoryType.FOOD_CATEGORY, "FAST_FOOD"),
                category(CategoryType.TEXTURE, "SOFT"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("SALAD", List.of(
                category(CategoryType.FLAVOR, "FRESH"),
                category(CategoryType.COOKING_METHOD, "RAW_SALAD"),
                category(CategoryType.FOOD_CATEGORY, "WESTERN"),
                category(CategoryType.TEXTURE, "CRUNCHY"),
                category(CategoryType.TEMPERATURE, "COLD")
        ));
        createdCount += mapAttributes("STEAK", List.of(
                category(CategoryType.FLAVOR, "RICH"),
                category(CategoryType.COOKING_METHOD, "GRILLED"),
                category(CategoryType.FOOD_CATEGORY, "WESTERN"),
                category(CategoryType.TEXTURE, "SOFT"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("PHO", List.of(
                category(CategoryType.FLAVOR, "FRESH"),
                category(CategoryType.COOKING_METHOD, "SOUP"),
                category(CategoryType.COOKING_METHOD, "NOODLE_MIXED"),
                category(CategoryType.FOOD_CATEGORY, "ASIAN"),
                category(CategoryType.TEXTURE, "CHEWY"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("PAD_THAI", List.of(
                category(CategoryType.FLAVOR, "SWEET"),
                category(CategoryType.FLAVOR, "FRESH"),
                category(CategoryType.COOKING_METHOD, "STIR_FRIED"),
                category(CategoryType.FOOD_CATEGORY, "ASIAN"),
                category(CategoryType.TEXTURE, "CHEWY"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("CURRY_RICE", List.of(
                category(CategoryType.FLAVOR, "RICH"),
                category(CategoryType.COOKING_METHOD, "STIR_FRIED"),
                category(CategoryType.FOOD_CATEGORY, "ASIAN"),
                category(CategoryType.TEXTURE, "SOFT"),
                category(CategoryType.TEMPERATURE, "HOT")
        ));
        createdCount += mapAttributes("BANH_MI", List.of(
                category(CategoryType.FLAVOR, "FRESH"),
                category(CategoryType.COOKING_METHOD, "RAW_SALAD"),
                category(CategoryType.FOOD_CATEGORY, "ASIAN"),
                category(CategoryType.TEXTURE, "CRISPY"),
                category(CategoryType.TEMPERATURE, "COLD")
        ));

        return createdCount;
    }

    private int createMenuIngredientMappings() {
        int createdCount = 0;

        createdCount += mapIngredients("KIMCHI_STEW", List.of("KIMCHI", "PORK", "TOFU", "GARLIC"));
        createdCount += mapIngredients("DOENJANG_STEW", List.of("SOYBEAN", "TOFU", "MUSHROOM", "GARLIC"));
        createdCount += mapIngredients("BIBIMBAP", List.of("EGG", "SESAME", "GARLIC"));
        createdCount += mapIngredients("PORK_CUTLET", List.of("PORK", "WHEAT", "EGG"));
        createdCount += mapIngredients("JJAJANGMYEON", List.of("WHEAT", "SOYBEAN", "PORK"));
        createdCount += mapIngredients("JJAMPPONG", List.of("WHEAT", "SHRIMP", "SQUID", "SHELLFISH"));
        createdCount += mapIngredients("RAMEN", List.of("WHEAT", "EGG", "PORK", "SOYBEAN"));
        createdCount += mapIngredients("SUSHI", List.of("FISH", "SHRIMP", "EGG"));
        createdCount += mapIngredients("BULGOGI", List.of("BEEF", "SOYBEAN", "SESAME", "GARLIC"));
        createdCount += mapIngredients("SAMGYEOPSAL", List.of("PORK", "GARLIC", "SESAME"));
        createdCount += mapIngredients("DAK_GALBI", List.of("CHICKEN", "SOYBEAN", "GARLIC"));
        createdCount += mapIngredients("BUDAE_JJIGAE", List.of("PORK", "KIMCHI", "WHEAT", "CHEESE"));
        createdCount += mapIngredients("GALBITANG", List.of("BEEF", "GARLIC"));
        createdCount += mapIngredients("NAENGMYEON", List.of("BUCKWHEAT", "EGG", "BEEF"));
        createdCount += mapIngredients("GIMBAP", List.of("EGG", "SESAME"));
        createdCount += mapIngredients("TTEOKBOKKI", List.of("WHEAT", "SOYBEAN"));
        createdCount += mapIngredients("UDON", List.of("WHEAT", "SOYBEAN"));
        createdCount += mapIngredients("SOBA", List.of("BUCKWHEAT", "SOYBEAN"));
        createdCount += mapIngredients("GYUDON", List.of("BEEF", "SOYBEAN", "EGG"));
        createdCount += mapIngredients("KARAAGE", List.of("CHICKEN", "WHEAT", "EGG", "SOYBEAN"));
        createdCount += mapIngredients("MAPO_TOFU", List.of("TOFU", "SOYBEAN", "PORK", "GARLIC"));
        createdCount += mapIngredients("TANGSUYUK", List.of("PORK", "WHEAT", "EGG"));
        createdCount += mapIngredients("FRIED_RICE", List.of("EGG", "SHRIMP", "PORK"));
        createdCount += mapIngredients("MALA_XIANG_GUO", List.of("BEEF", "SHRIMP", "SOYBEAN", "GARLIC"));
        createdCount += mapIngredients("PASTA", List.of("WHEAT", "MILK", "CHEESE"));
        createdCount += mapIngredients("PIZZA", List.of("WHEAT", "MILK", "CHEESE"));
        createdCount += mapIngredients("HAMBURGER", List.of("WHEAT", "BEEF", "CHEESE", "EGG"));
        createdCount += mapIngredients("SALAD", List.of("EGG", "CHEESE"));
        createdCount += mapIngredients("STEAK", List.of("BEEF", "GARLIC"));
        createdCount += mapIngredients("PHO", List.of("BEEF", "CILANTRO"));
        createdCount += mapIngredients("PAD_THAI", List.of("SHRIMP", "PEANUT", "EGG", "SOYBEAN"));
        createdCount += mapIngredients("CURRY_RICE", List.of("BEEF", "PORK", "MILK"));
        createdCount += mapIngredients("BANH_MI", List.of("WHEAT", "PORK", "CILANTRO"));

        return createdCount;
    }

    private int mapAttributes(String menuCode, List<CategoryKey> categoryKeys) {
        MenuItem menuItem = menuItem(menuCode);
        int createdCount = 0;

        for (CategoryKey categoryKey : categoryKeys) {
            AttributeCategory attributeCategory = attributeCategory(categoryKey.categoryType(), categoryKey.code());
            if (menuAttributeCategoryRepository.existsByMenuAndAttributeCategory(menuItem, attributeCategory)) {
                continue;
            }

            menuAttributeCategoryRepository.save(new MenuAttributeCategory(menuItem, attributeCategory));
            createdCount++;
        }

        return createdCount;
    }

    private int mapIngredients(String menuCode, List<String> ingredientCodes) {
        MenuItem menuItem = menuItem(menuCode);
        int createdCount = 0;

        for (String ingredientCode : ingredientCodes) {
            Ingredient ingredient = ingredient(ingredientCode);
            if (menuIngredientRepository.existsByMenuAndIngredient(menuItem, ingredient)) {
                continue;
            }

            menuIngredientRepository.save(new MenuIngredient(menuItem, ingredient));
            createdCount++;
        }

        return createdCount;
    }

    private int createAttributeCategoryIfAbsent(CategoryType categoryType, String code, String name, int sortOrder) {
        return attributeCategoryRepository.findByCategoryTypeAndCode(categoryType, code)
                .map(attributeCategory -> {
                    attributeCategory.updateName(name);
                    attributeCategory.updateSortOrder(sortOrder);
                    attributeCategory.activate();
                    log.info("Attribute category already exists. categoryType={}, code={}", categoryType, code);
                    return 0;
                })
                .orElseGet(() -> {
                    attributeCategoryRepository.save(new AttributeCategory(categoryType, code, name, sortOrder));
                    log.info("Attribute category created. categoryType={}, code={}", categoryType, code);
                    return 1;
                });
    }

    private int createIngredientIfAbsent(String code, String name, boolean allergen, int sortOrder) {
        return ingredientRepository.findByCode(code)
                .map(ingredient -> {
                    ingredient.updateName(name);
                    ingredient.updateAllergen(allergen);
                    ingredient.updateSortOrder(sortOrder);
                    ingredient.activate();
                    log.info("Ingredient already exists. code={}", code);
                    return 0;
                })
                .orElseGet(() -> {
                    ingredientRepository.save(new Ingredient(code, name, allergen, sortOrder));
                    log.info("Ingredient created. code={}", code);
                    return 1;
                });
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

    private CategoryKey category(CategoryType categoryType, String code) {
        return new CategoryKey(categoryType, code);
    }

    private AttributeCategory attributeCategory(CategoryType categoryType, String code) {
        return attributeCategoryRepository.findByCategoryTypeAndCode(categoryType, code)
                .orElseThrow(() -> new IllegalStateException(
                        "Attribute category seed missing. categoryType=" + categoryType + ", code=" + code
                ));
    }

    private Ingredient ingredient(String code) {
        return ingredientRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Ingredient seed missing. code=" + code));
    }

    private MenuItem menuItem(String code) {
        return menuItemRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Menu item seed missing. code=" + code));
    }

    private record CategoryKey(CategoryType categoryType, String code) {
    }
}
