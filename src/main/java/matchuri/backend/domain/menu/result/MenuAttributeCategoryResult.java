package matchuri.backend.domain.menu.result;

import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;

public record MenuAttributeCategoryResult(
        Long id, CategoryType categoryType, String code, String name, int sortOrder
) {
    public static MenuAttributeCategoryResult from(AttributeCategory category) {
        return new MenuAttributeCategoryResult(category.getId(), category.getCategoryType(),
                category.getCode(), category.getName(), category.getSortOrder());
    }
}
