package matchuri.backend.recommendation.algorithm.personal;

import java.util.List;
import java.util.Map;
import matchuri.backend.catalog.entity.CategoryType;
import matchuri.backend.recommendation.algorithm.RecommendationAlgorithmType;
import matchuri.backend.recommendation.algorithm.input.MenuRecommendationProfile;
import matchuri.backend.recommendation.algorithm.input.TasteProfileSnapshot;
import matchuri.backend.recommendation.algorithm.support.RecommendationPreferenceMatchCalculator;
import matchuri.backend.recommendation.algorithm.support.RecommendationScoreNormalizer;
import matchuri.backend.recommendation.algorithm.support.SingleParticipantMenuRecommendationAlgorithm;
import org.springframework.stereotype.Component;

@Component
public class PersonalMenuRecommendationAlgorithmV1 extends SingleParticipantMenuRecommendationAlgorithm {

    private static final String VERSION = "v1.3";
    private static final double SCORE_MAX = 100.0;
    private static final double HISTORY_BONUS_RATIO = 0.5;

    @Override
    public RecommendationAlgorithmType type() {
        return RecommendationAlgorithmType.PERSONAL;
    }

    @Override
    public String version() {
        return VERSION;
    }

    @Override
    protected ScoredMenu score(
            MenuRecommendationProfile menu,
            TasteProfileSnapshot participant,
            Map<Long, Long> selectedAttributeCategoryFrequency,
            Map<Long, CategoryType> categoryTypes
    ) {
        var preferenceMatch = calculatePreferenceMatch(
                menu.attributeCategoryIds(), participant.preferredAttributeCategoryIds(), categoryTypes);
        long historyMatchingCount = menu.attributeCategoryIds().stream()
                .distinct()
                .mapToLong(categoryId -> selectedAttributeCategoryFrequency.getOrDefault(categoryId, 0L))
                .sum();
        double historyMatchRatio = calculateHistoryMatchRatio(historyMatchingCount, selectedAttributeCategoryFrequency);
        boolean hasPreferences = preferenceMatch.preferredUnitCount() > 0;
        double preferenceScore = hasPreferences
                ? SCORE_MAX * preferenceMatch.matchingCount() / preferenceMatch.preferredUnitCount() : 0;
        double historyScore = hasPreferences
                ? (SCORE_MAX - preferenceScore) * HISTORY_BONUS_RATIO * historyMatchRatio
                : SCORE_MAX * historyMatchRatio;
        double finalScore = RecommendationScoreNormalizer.normalize(preferenceScore + historyScore, SCORE_MAX);

        return new ScoredMenu(menu, preferenceMatch.matchingCount(), historyMatchingCount,
                preferenceScore, historyScore, finalScore, finalScore);
    }

    private double calculateHistoryMatchRatio(
            long historyMatchingCount,
            Map<Long, Long> selectedAttributeCategoryFrequency
    ) {
        long totalFrequency = selectedAttributeCategoryFrequency.values().stream().mapToLong(Long::longValue).sum();
        return totalFrequency <= 0 ? 0 : Math.clamp((double) historyMatchingCount / totalFrequency, 0.0, 1.0);
    }

    @Override
    protected RecommendationPreferenceMatchCalculator.PreferenceMatch calculatePreferenceMatch(
            List<Long> menuAttributeIds,
            List<Long> preferredAttributeIds,
            Map<Long, CategoryType> categoryTypes
    ) {
        return RecommendationPreferenceMatchCalculator.calculateByCategoryType(
                menuAttributeIds, preferredAttributeIds, categoryTypes);
    }
}
