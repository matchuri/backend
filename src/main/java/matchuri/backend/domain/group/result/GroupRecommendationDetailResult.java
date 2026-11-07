package matchuri.backend.domain.group.result;

import java.util.List;

public record GroupRecommendationDetailResult(
        GroupRecommendationResult session,
        List<GroupRecommendationCategoryResult> recommendationCategories
) {
}
