package matchuri.backend.domain.recommendation.result;

import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.menu.result.MenuAttributeCategoryResult;
import org.jspecify.annotations.Nullable;

public record PersonalRecommendationHomeResult(
        @Nullable PersonalRecommendationSummaryResult latestRecommendation,
        List<SelectedRecommendation> selectedRecommendations
) {
    public record SelectedRecommendation(
            Long id,
            LocalDateTime createdAt,
            String menuName,
            List<MenuAttributeCategoryResult> attributeCategories
    ) {
    }
}
