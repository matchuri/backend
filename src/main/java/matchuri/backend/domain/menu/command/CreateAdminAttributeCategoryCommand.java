package matchuri.backend.domain.menu.command;

import matchuri.backend.domain.menu.entity.CategoryType;

public record CreateAdminAttributeCategoryCommand(
        CategoryType categoryType,
        String code,
        String name,
        int sortOrder
) {
}
