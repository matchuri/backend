package matchuri.backend.domain.menu.command;

import java.util.List;

public record CreateAdminMenuItemCommand(
        String code,
        String name,
        String description,
        List<Long> attributeCategoryIds,
        List<Long> ingredientIds
) {
    public CreateAdminMenuItemCommand {
        attributeCategoryIds = attributeCategoryIds == null ? List.of() : List.copyOf(attributeCategoryIds);
        ingredientIds = ingredientIds == null ? List.of() : List.copyOf(ingredientIds);
    }
}
