package matchuri.backend.domain.menu.repository;

import matchuri.backend.domain.menu.entity.CategoryType;

public record MenuAttributeCategoryRow(
        Long id,
        CategoryType categoryType,
        String code,
        String name,
        Integer sortOrder
) {
}
