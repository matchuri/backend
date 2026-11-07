package matchuri.backend.domain.menu.result;

import matchuri.backend.domain.menu.entity.Ingredient;

public record RestrictionIngredientResult(
        Long id,
        String code,
        String name,
        boolean allergen,
        int sortOrder
) {

    public static RestrictionIngredientResult from(Ingredient ingredient) {
        return new RestrictionIngredientResult(
                ingredient.getId(),
                ingredient.getCode(),
                ingredient.getName(),
                ingredient.isAllergen(),
                ingredient.getSortOrder()
        );
    }
}
