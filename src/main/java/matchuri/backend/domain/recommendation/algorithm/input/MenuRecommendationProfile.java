package matchuri.backend.domain.recommendation.algorithm.input;

import java.util.List;

public record MenuRecommendationProfile(
        Long menuId,
        String menuCode,
        String menuName,
        List<Long> attributeCategoryIds,
        List<Long> ingredientIds
) {
    public MenuRecommendationProfile {
        attributeCategoryIds = attributeCategoryIds == null ? List.of() : List.copyOf(attributeCategoryIds);
        ingredientIds = ingredientIds == null ? List.of() : List.copyOf(ingredientIds);
    }
}
