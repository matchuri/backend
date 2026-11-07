package matchuri.backend.infra.seed;

import java.util.List;
import matchuri.backend.domain.menu.entity.CategoryType;

public record ReferenceSeedData(
        List<AttributeCategorySeed> attributeCategories,
        List<IngredientSeed> ingredients,
        List<MenuItemSeed> menuItems,
        List<MenuAttributeCategorySeed> menuAttributeCategories,
        List<MenuIngredientSeed> menuIngredients
) {

    public record AttributeCategorySeed(
            CategoryType categoryType,
            String code,
            String name,
            int sortOrder
    ) {
    }

    public record IngredientSeed(
            String code,
            String name,
            boolean allergen,
            int sortOrder
    ) {
    }

    public record MenuItemSeed(
            String code,
            String name,
            String description
    ) {
    }

    public record MenuAttributeCategorySeed(
            String menuCode,
            CategoryType categoryType,
            String categoryCode
    ) {
    }

    public record MenuIngredientSeed(
            String menuCode,
            String ingredientCode
    ) {
    }
}
