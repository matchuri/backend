package matchuri.backend.domain.menu.result;

import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;

public record AttributeCategoryResult(
        Long id,
        CategoryType categoryType,
        String code,
        String name,
        int sortOrder
) {

    public static AttributeCategoryResult from(AttributeCategory attributeCategory) {
        return new AttributeCategoryResult(
                attributeCategory.getId(),
                attributeCategory.getCategoryType(),
                attributeCategory.getCode(),
                attributeCategory.getName(),
                attributeCategory.getSortOrder()
        );
    }
}
