package matchuri.backend.domain.recommendation.algorithm.personal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.recommendation.algorithm.RecommendationAlgorithmType;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationInput;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationProfile;
import matchuri.backend.domain.recommendation.algorithm.input.TasteProfileSnapshot;
import matchuri.backend.domain.recommendation.algorithm.support.RecommendationPreferenceMatchCalculator;
import matchuri.backend.domain.recommendation.algorithm.support.RecommendationScoreNormalizer;
import matchuri.backend.domain.recommendation.algorithm.support.SingleParticipantMenuRecommendationAlgorithm;
import org.springframework.stereotype.Component;

@Component
public class PersonalMenuRecommendationAlgorithmV1 extends SingleParticipantMenuRecommendationAlgorithm {

    private static final String VERSION = "v1.5";
    private static final double SCORE_MAX = 100.0;
    private static final double HISTORY_BONUS_RATIO = 0.5;
    private static final int DIVERSITY_REPLACEMENT_RANK = 3;
    private static final double MAX_DIVERSITY_SCORE_GAP = 10.0;

    @Override
    public RecommendationAlgorithmType type() {
        return RecommendationAlgorithmType.PERSONAL;
    }

    @Override
    public String version() {
        return VERSION;
    }

    @Override
    protected List<ScoredMenu> selectCandidates(Stream<ScoredMenu> rankedMenus, MenuRecommendationInput input) {
        int limit = input.candidateLimit();
        if (limit <= 1 || input.attributeCategoryTypes().isEmpty()) {
            return super.selectCandidates(rankedMenus, input);
        }

        var remaining = rankedMenus
                .map(menu -> DiversityCandidate.from(menu, input.attributeCategoryTypes()))
                .collect(Collectors.toCollection(ArrayList::new));
        List<ScoredMenu> selected = new ArrayList<>();
        Set<Long> selectedCookingMethods = new HashSet<>();
        Set<Long> selectedFoodCategories = new HashSet<>();
        boolean allSelectedCookingMethodsKnown = true;
        Comparator<DiversityCandidate> diversityOrder = Comparator
                .comparing((DiversityCandidate candidate) ->
                        addsDiversity(candidate.cookingMethods(), selectedCookingMethods), Comparator.reverseOrder())
                .thenComparing(candidate -> addsDiversity(candidate.foodCategories(), selectedFoodCategories),
                        Comparator.reverseOrder())
                .thenComparing(candidate -> candidate.scoredMenu().menu().menuId());

        while (selected.size() < limit && !remaining.isEmpty()) {
            double highestScore = remaining.getFirst().score();
            DiversityCandidate next = remaining.stream()
                    .takeWhile(candidate -> Double.compare(candidate.score(), highestScore) == 0)
                    .min(diversityOrder)
                    .orElseThrow();
            if (limit == DIVERSITY_REPLACEMENT_RANK && selected.size() == DIVERSITY_REPLACEMENT_RANK - 1
                    && allSelectedCookingMethodsKnown
                    && !Collections.disjoint(next.cookingMethods(), selectedCookingMethods)) {
                next = findDiverseThirdCandidate(remaining, next, selectedCookingMethods,
                        input.participants().getFirst().preferredAttributeCategoryIds(), diversityOrder);
            }
            selected.add(next.scoredMenu());
            selectedCookingMethods.addAll(next.cookingMethods());
            selectedFoodCategories.addAll(next.foodCategories());
            allSelectedCookingMethodsKnown &= !next.cookingMethods().isEmpty();
            remaining.remove(next);
        }
        return selected;
    }

    private DiversityCandidate findDiverseThirdCandidate(
            List<DiversityCandidate> remaining,
            DiversityCandidate regularThird,
            Set<Long> selectedCookingMethods,
            List<Long> preferredAttributeIds,
            Comparator<DiversityCandidate> diversityOrder
    ) {
        double minimumScore = RecommendationScoreNormalizer.normalize(
                regularThird.score() - MAX_DIVERSITY_SCORE_GAP, SCORE_MAX);
        boolean matchesPreferredCooking = !Collections.disjoint(regularThird.cookingMethods(), preferredAttributeIds);
        return remaining.stream()
                .takeWhile(candidate -> candidate.score() >= minimumScore)
                .filter(candidate -> candidate.score() < regularThird.score())
                .filter(candidate -> addsDiversity(candidate.cookingMethods(), selectedCookingMethods))
                .filter(candidate -> !matchesPreferredCooking
                        || !Collections.disjoint(candidate.cookingMethods(), preferredAttributeIds))
                .min(Comparator.comparingDouble(DiversityCandidate::score).reversed().thenComparing(diversityOrder))
                .orElse(regularThird);
    }

    private static boolean addsDiversity(Set<Long> candidateTags, Set<Long> selectedTags) {
        return !candidateTags.isEmpty() && !selectedTags.isEmpty() && Collections.disjoint(candidateTags, selectedTags);
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

    private record DiversityCandidate(ScoredMenu scoredMenu, Set<Long> cookingMethods, Set<Long> foodCategories) {

        private double score() {
            return scoredMenu.totalScore();
        }

        private static DiversityCandidate from(ScoredMenu scoredMenu, Map<Long, CategoryType> categoryTypes) {
            Set<Long> cookingMethods = new HashSet<>();
            Set<Long> foodCategories = new HashSet<>();
            for (Long attributeId : scoredMenu.menu().attributeCategoryIds()) {
                CategoryType type = categoryTypes.get(attributeId);
                if (type == CategoryType.COOKING_METHOD) {
                    cookingMethods.add(attributeId);
                } else if (type == CategoryType.FOOD_CATEGORY) {
                    foodCategories.add(attributeId);
                }
            }
            return new DiversityCandidate(scoredMenu, cookingMethods, foodCategories);
        }
    }
}
