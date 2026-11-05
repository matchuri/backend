package matchuri.backend.domain.member.repository;

import matchuri.backend.domain.menu.entity.CategoryType;

public record MemberTasteProfileAttributeCategoryRow(
        Long id,
        CategoryType categoryType,
        String code,
        String name,
        Integer sortOrder
) {
}
