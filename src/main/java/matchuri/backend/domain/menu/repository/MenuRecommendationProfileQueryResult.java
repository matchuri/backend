package matchuri.backend.domain.menu.repository;

import java.util.List;

public record MenuRecommendationProfileQueryResult(
        Long menuId,
        String menuCode,
        String menuName,
        List<Long> attributeCategoryIds,
        List<Long> ingredientIds
) {
}
