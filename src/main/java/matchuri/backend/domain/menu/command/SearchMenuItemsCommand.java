package matchuri.backend.domain.menu.command;

import java.util.List;

public record SearchMenuItemsCommand(
        String query,
        List<Long> attributeCategoryIds,
        List<Long> ingredientIds
) {
}
