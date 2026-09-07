package matchuri.backend.domain.menu.repository;

import java.util.List;
import matchuri.backend.domain.menu.entity.CategoryType;

public record MenuItemDetailQueryResult(
        Long id,
        String code,
        String name,
        String description,
        String thumbnailObjectKey,
        List<AttributeCategoryRow> attributeCategories,
        List<RestrictionIngredientRow> ingredients
) {

    public record AttributeCategoryRow(
            Long id,
            CategoryType categoryType,
            String code,
            String name,
            Integer sortOrder
    ) {
    }

    public record RestrictionIngredientRow(
            Long id,
            String code,
            String name,
            Boolean allergen,
            Integer sortOrder
    ) {
    }
}
