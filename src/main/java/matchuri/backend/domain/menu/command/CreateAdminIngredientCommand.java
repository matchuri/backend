package matchuri.backend.domain.menu.command;

public record CreateAdminIngredientCommand(
        String code,
        String name,
        boolean allergen,
        int sortOrder
) {
}
