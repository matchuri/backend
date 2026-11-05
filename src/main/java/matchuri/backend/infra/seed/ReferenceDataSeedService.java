package matchuri.backend.infra.seed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferenceDataSeedService {

    private static final String RESOURCE_PATH = "seed/reference-data.json";

    private final SeedDataResourceLoader resourceLoader;
    private final AttributeCategoryRepository attributeCategoryRepository;
    private final IngredientRepository ingredientRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuAttributeCategoryRepository menuAttributeCategoryRepository;
    private final MenuIngredientRepository menuIngredientRepository;

    @Transactional
    public void initialize() {
        ReferenceSeedData seedData = resourceLoader.load(RESOURCE_PATH, ReferenceSeedData.class);

        Map<AttributeCategoryKey, AttributeCategory> categories = seedAttributeCategories(seedData);
        Map<String, Ingredient> ingredients = seedIngredients(seedData);
        Map<String, MenuItem> menuItems = seedMenuItems(seedData);
        int createdCategoryMappings = seedMenuAttributeCategories(seedData, menuItems, categories);
        int createdIngredientMappings = seedMenuIngredients(seedData, menuItems, ingredients);

        log.info(
                "Reference seed initialization completed. categories={}, ingredients={}, menus={}, "
                        + "createdCategoryMappings={}, createdIngredientMappings={}",
                seedData.attributeCategories().size(),
                seedData.ingredients().size(),
                seedData.menuItems().size(),
                createdCategoryMappings,
                createdIngredientMappings
        );
    }

    private Map<AttributeCategoryKey, AttributeCategory> seedAttributeCategories(ReferenceSeedData seedData) {
        Map<AttributeCategoryKey, AttributeCategory> categories = new HashMap<>();
        attributeCategoryRepository.findAll().forEach(category -> putUnique(
                categories,
                new AttributeCategoryKey(category.getCategoryType(), category.getCode()),
                category,
                "DB attribute category"
        ));

        List<AttributeCategory> created = new ArrayList<>();
        for (ReferenceSeedData.AttributeCategorySeed seed : seedData.attributeCategories()) {
            AttributeCategoryKey key = new AttributeCategoryKey(seed.categoryType(), seed.code());
            if (!categories.containsKey(key)) {
                created.add(new AttributeCategory(seed.categoryType(), seed.code(), seed.name(), seed.sortOrder()));
            }
        }

        attributeCategoryRepository.saveAll(created).forEach(category -> categories.put(
                new AttributeCategoryKey(category.getCategoryType(), category.getCode()),
                category
        ));
        requireCoverage(seedData.attributeCategories().stream()
                .map(seed -> new AttributeCategoryKey(seed.categoryType(), seed.code()))
                .toList(), categories.keySet(), "attribute category");
        return categories;
    }

    private Map<String, Ingredient> seedIngredients(ReferenceSeedData seedData) {
        Map<String, Ingredient> ingredients = new HashMap<>();
        ingredientRepository.findAll().forEach(ingredient -> putUnique(
                ingredients,
                ingredient.getCode(),
                ingredient,
                "DB ingredient"
        ));

        List<Ingredient> created = seedData.ingredients().stream()
                .filter(seed -> !ingredients.containsKey(seed.code()))
                .map(seed -> new Ingredient(seed.code(), seed.name(), seed.allergen(), seed.sortOrder()))
                .toList();
        ingredientRepository.saveAll(created).forEach(ingredient -> ingredients.put(ingredient.getCode(), ingredient));
        requireCoverage(seedData.ingredients().stream().map(ReferenceSeedData.IngredientSeed::code).toList(),
                ingredients.keySet(), "ingredient");
        return ingredients;
    }

    private Map<String, MenuItem> seedMenuItems(ReferenceSeedData seedData) {
        Map<String, MenuItem> menuItems = new HashMap<>();
        menuItemRepository.findAll().forEach(menuItem -> putUnique(
                menuItems,
                menuItem.getCode(),
                menuItem,
                "DB menu item"
        ));

        List<MenuItem> created = seedData.menuItems().stream()
                .filter(seed -> !menuItems.containsKey(seed.code()))
                .map(seed -> new MenuItem(seed.code(), seed.name(), seed.description()))
                .toList();
        menuItemRepository.saveAll(created).forEach(menuItem -> menuItems.put(menuItem.getCode(), menuItem));
        requireCoverage(seedData.menuItems().stream().map(ReferenceSeedData.MenuItemSeed::code).toList(),
                menuItems.keySet(), "menu item");
        return menuItems;
    }

    private int seedMenuAttributeCategories(
            ReferenceSeedData seedData,
            Map<String, MenuItem> menuItems,
            Map<AttributeCategoryKey, AttributeCategory> categories
    ) {
        Set<MenuAttributeCategoryKey> mappings = new HashSet<>();
        menuAttributeCategoryRepository.findAll().forEach(mapping -> mappings.add(new MenuAttributeCategoryKey(
                mapping.getMenu().getCode(),
                mapping.getAttributeCategory().getCategoryType(),
                mapping.getAttributeCategory().getCode()
        )));

        List<MenuAttributeCategory> created = new ArrayList<>();
        for (ReferenceSeedData.MenuAttributeCategorySeed seed : seedData.menuAttributeCategories()) {
            MenuAttributeCategoryKey key = new MenuAttributeCategoryKey(
                    seed.menuCode(),
                    seed.categoryType(),
                    seed.categoryCode()
            );
            if (mappings.add(key)) {
                created.add(new MenuAttributeCategory(
                        require(menuItems, seed.menuCode(), "menu item"),
                        require(categories, new AttributeCategoryKey(seed.categoryType(), seed.categoryCode()),
                                "attribute category")
                ));
            }
        }

        menuAttributeCategoryRepository.saveAll(created);
        requireCoverage(seedData.menuAttributeCategories().stream()
                        .map(seed -> new MenuAttributeCategoryKey(
                                seed.menuCode(),
                                seed.categoryType(),
                                seed.categoryCode()
                        ))
                        .toList(),
                mappings,
                "menu attribute category mapping");
        return created.size();
    }

    private int seedMenuIngredients(
            ReferenceSeedData seedData,
            Map<String, MenuItem> menuItems,
            Map<String, Ingredient> ingredients
    ) {
        Set<MenuIngredientKey> mappings = new HashSet<>();
        menuIngredientRepository.findAll().forEach(mapping -> mappings.add(new MenuIngredientKey(
                mapping.getMenu().getCode(),
                mapping.getIngredient().getCode()
        )));

        List<MenuIngredient> created = new ArrayList<>();
        for (ReferenceSeedData.MenuIngredientSeed seed : seedData.menuIngredients()) {
            MenuIngredientKey key = new MenuIngredientKey(seed.menuCode(), seed.ingredientCode());
            if (mappings.add(key)) {
                created.add(new MenuIngredient(
                        require(menuItems, seed.menuCode(), "menu item"),
                        require(ingredients, seed.ingredientCode(), "ingredient")
                ));
            }
        }

        menuIngredientRepository.saveAll(created);
        requireCoverage(seedData.menuIngredients().stream()
                        .map(seed -> new MenuIngredientKey(seed.menuCode(), seed.ingredientCode()))
                        .toList(),
                mappings,
                "menu ingredient mapping");
        return created.size();
    }

    private <K, V> void putUnique(Map<K, V> values, K key, V value, String target) {
        if (values.put(key, value) != null) {
            throw new IllegalStateException(target + " key가 중복되었습니다. key=" + key);
        }
    }

    private <K, V> V require(Map<K, V> values, K key, String target) {
        V value = values.get(key);
        if (value == null) {
            throw new IllegalStateException("Seed data가 참조하는 " + target + "을 찾을 수 없습니다. key=" + key);
        }
        return value;
    }

    private <T> void requireCoverage(List<T> required, Set<T> actual, String target) {
        Set<T> uniqueRequired = new HashSet<>(required);
        if (uniqueRequired.size() != required.size()) {
            throw new IllegalStateException("Seed resource의 " + target + " key가 중복되었습니다.");
        }
        if (!actual.containsAll(uniqueRequired)) {
            throw new IllegalStateException("Seed initialization 후 " + target + "이 누락되었습니다.");
        }
    }

    private record AttributeCategoryKey(CategoryType categoryType, String code) {
    }

    private record MenuAttributeCategoryKey(String menuCode, CategoryType categoryType, String categoryCode) {
    }

    private record MenuIngredientKey(String menuCode, String ingredientCode) {
    }
}
