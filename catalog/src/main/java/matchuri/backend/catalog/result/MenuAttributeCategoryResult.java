package matchuri.backend.catalog.result;

import matchuri.backend.catalog.entity.AttributeCategory;
import matchuri.backend.catalog.entity.CategoryType;

public record MenuAttributeCategoryResult(
        Long id, CategoryType categoryType, String code, String name, int sortOrder
) {
    public static MenuAttributeCategoryResult from(AttributeCategory category) {
        return new MenuAttributeCategoryResult(category.getId(), category.getCategoryType(),
                category.getCode(), category.getName(), category.getSortOrder());
    }
}
