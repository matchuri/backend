package matchuri.backend.domain.menu.result;

import java.util.List;

public record AdminMenuItemDetailResult(
        Long id,
        String code,
        String name,
        String description,
        boolean isActive,
        String thumbnailUrl,
        List<AdminAttributeCategoryResult> attributeCategories,
        List<AdminIngredientResult> ingredients
) {
    public AdminMenuItemDetailResult {
        attributeCategories = attributeCategories == null ? List.of() : List.copyOf(attributeCategories);
        ingredients = ingredients == null ? List.of() : List.copyOf(ingredients);
    }
}
