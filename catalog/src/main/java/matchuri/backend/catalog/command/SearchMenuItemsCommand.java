package matchuri.backend.catalog.command;

import java.util.List;

public record SearchMenuItemsCommand(
        String query,
        List<Long> attributeCategoryIds,
        List<Long> ingredientIds
) {
}
