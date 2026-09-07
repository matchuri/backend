package matchuri.backend.domain.menu.result;

import java.util.List;

public record MenuItemDetailResult(
        Long id,
        String code,
        String name,
        String description,
        String thumbnailUrl,
        List<AttributeCategoryResult> attributeCategories,
        List<RestrictionIngredientResult> ingredients
) {
}
