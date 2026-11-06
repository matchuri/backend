package matchuri.backend.domain.group.result;

import matchuri.backend.domain.group.entity.GroupRecommendationCategory;
import matchuri.backend.domain.group.entity.GroupRecommendationCategorySource;
import matchuri.backend.domain.menu.entity.CategoryType;

public record GroupRecommendationCategoryResult(
        Long id,
        CategoryType categoryType,
        String code,
        String name,
        int rankNo,
        GroupRecommendationCategorySource source
) {
    public static GroupRecommendationCategoryResult from(GroupRecommendationCategory category) {
        return new GroupRecommendationCategoryResult(
                category.getAttributeCategory().getId(),
                category.getAttributeCategory().getCategoryType(),
                category.getAttributeCategory().getCode(),
                category.getAttributeCategory().getName(),
                category.getRankNo(),
                category.getSource()
        );
    }
}
