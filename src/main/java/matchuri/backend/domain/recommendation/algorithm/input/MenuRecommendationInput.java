package matchuri.backend.domain.recommendation.algorithm.input;

import java.util.List;
import java.util.Map;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.recommendation.algorithm.RecommendationTargetType;

public record MenuRecommendationInput(
        RecommendationTargetType targetType,
        List<TasteProfileSnapshot> participants,
        List<MenuRecommendationProfile> menus,
        RecommendationContextSnapshot context,
        int candidateLimit,
        List<Long> recentSelectedMenuIds,
        List<Long> recentlySkippedMenuIds,
        Map<Long, Long> selectedAttributeCategoryFrequency,
        Map<Long, CategoryType> attributeCategoryTypes
) {
    /** 유형 정보가 없는 내부 입력은 기존 ID 단위 계산을 유지한다. 실제 API 유스케이스는 유형을 전달한다. */
    public MenuRecommendationInput(
            RecommendationTargetType targetType,
            List<TasteProfileSnapshot> participants,
            List<MenuRecommendationProfile> menus,
            RecommendationContextSnapshot context,
            int candidateLimit,
            List<Long> recentSelectedMenuIds,
            List<Long> recentlySkippedMenuIds,
            Map<Long, Long> selectedAttributeCategoryFrequency
    ) {
        this(targetType, participants, menus, context, candidateLimit, recentSelectedMenuIds,
                recentlySkippedMenuIds, selectedAttributeCategoryFrequency, Map.of());
    }

    public MenuRecommendationInput {
        participants = participants == null ? List.of() : List.copyOf(participants);
        menus = menus == null ? List.of() : List.copyOf(menus);
        context = context == null ? RecommendationContextSnapshot.of(null) : context;
        recentSelectedMenuIds = recentSelectedMenuIds == null ? List.of() : List.copyOf(recentSelectedMenuIds);
        recentlySkippedMenuIds = recentlySkippedMenuIds == null ? List.of() : List.copyOf(recentlySkippedMenuIds);
        selectedAttributeCategoryFrequency = selectedAttributeCategoryFrequency == null
                ? Map.of()
                : Map.copyOf(selectedAttributeCategoryFrequency);
        attributeCategoryTypes = attributeCategoryTypes == null ? Map.of() : Map.copyOf(attributeCategoryTypes);
    }
}
