package matchuri.backend.domain.menu.result;

import java.util.List;
import matchuri.backend.domain.menu.entity.MenuItem;

public record MenuItemDetailResult(
        Long id,
        String code,
        String name,
        String description,
        List<AttributeCategoryResult> attributeCategories,
        List<RestrictionIngredientResult> ingredients
) {

    public static MenuItemDetailResult of(
            MenuItem menuItem,
            List<AttributeCategoryResult> attributeCategories,
            List<RestrictionIngredientResult> ingredients
    ) {
        return new MenuItemDetailResult(
                menuItem.getId(),
                menuItem.getCode(),
                menuItem.getName(),
                menuItem.getDescription(),
                attributeCategories,
                ingredients
        );
    }
}
