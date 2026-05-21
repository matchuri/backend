package matchuri.backend.domain.recommendation.algorithm.input;

import java.util.List;
import java.util.Map;
import matchuri.backend.domain.recommendation.algorithm.RecommendationTargetType;

public record MenuRecommendationInput(
        RecommendationTargetType targetType,
        List<TasteProfileSnapshot> participants,
        List<MenuRecommendationProfile> menus,
        RecommendationContextSnapshot context,
        int candidateLimit,
        List<Long> recentSelectedMenuIds,
        List<Long> recentlySkippedMenuIds,
        Map<Long, Long> selectedAttributeCategoryFrequency
) {
    public MenuRecommendationInput {
        participants = participants == null ? List.of() : List.copyOf(participants);
        menus = menus == null ? List.of() : List.copyOf(menus);
        context = context == null ? RecommendationContextSnapshot.of(null) : context;
        recentSelectedMenuIds = recentSelectedMenuIds == null ? List.of() : List.copyOf(recentSelectedMenuIds);
        recentlySkippedMenuIds = recentlySkippedMenuIds == null ? List.of() : List.copyOf(recentlySkippedMenuIds);
        selectedAttributeCategoryFrequency = selectedAttributeCategoryFrequency == null
                ? Map.of()
                : Map.copyOf(selectedAttributeCategoryFrequency);
    }
}
