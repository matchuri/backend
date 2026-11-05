package matchuri.backend.domain.menu.command;

import java.util.List;

public record UpdateAdminMenuItemReferencesCommand(
        Long menuItemId,
        List<Long> attributeCategoryIds,
        List<Long> ingredientIds
) {
    public UpdateAdminMenuItemReferencesCommand {
        attributeCategoryIds = attributeCategoryIds == null ? List.of() : List.copyOf(attributeCategoryIds);
        ingredientIds = ingredientIds == null ? List.of() : List.copyOf(ingredientIds);
    }
}
