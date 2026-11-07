package matchuri.backend.domain.menu.repository;

public record MenuRestrictionIngredientRow(
        Long id,
        String code,
        String name,
        Boolean allergen,
        Integer sortOrder
) {
}
