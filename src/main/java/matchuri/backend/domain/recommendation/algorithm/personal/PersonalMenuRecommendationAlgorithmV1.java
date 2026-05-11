package matchuri.backend.domain.recommendation.algorithm.personal;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import matchuri.backend.domain.recommendation.algorithm.MenuRecommendationAlgorithm;
import matchuri.backend.domain.recommendation.algorithm.RecommendationAlgorithmType;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationInput;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationProfile;
import matchuri.backend.domain.recommendation.algorithm.input.TasteProfileSnapshot;
import matchuri.backend.domain.recommendation.algorithm.output.MenuRecommendationCandidateResult;
import matchuri.backend.domain.recommendation.algorithm.output.MenuRecommendationResult;
import org.springframework.stereotype.Component;

@Component
public class PersonalMenuRecommendationAlgorithmV1 implements MenuRecommendationAlgorithm {

    private static final String VERSION = "v1";

    @Override
    public RecommendationAlgorithmType type() {
        return RecommendationAlgorithmType.PERSONAL;
    }

    @Override
    public String version() {
        return VERSION;
    }

    /**
     * 회원의 제한 재료, 비선호 메뉴, 최근 선택 메뉴를 제외한 뒤 취향과 선택 이력 기반 점수로 개인 추천 후보를 만든다.
     *
     * @param input 개인 추천에 필요한 취향 profile과 메뉴 후보 snapshot
     * @return 점수순으로 정렬된 개인 추천 후보 결과
     */
    @Override
    public MenuRecommendationResult recommend(MenuRecommendationInput input) {
        TasteProfileSnapshot participant = input.participants().getFirst();

        List<ScoredMenu> scoredMenus = input.menus().stream()
                .filter(menu -> !containsAny(menu.ingredientIds(), participant.restrictionIngredientIds()))
                .filter(menu -> !participant.dislikedMenuItemIds().contains(menu.menuId()))
                .filter(menu -> !input.recentSelectedMenuIds().contains(menu.menuId()))
                .map(menu -> score(menu, participant, input.selectedAttributeCategoryFrequency()))
                .sorted(Comparator.comparing(ScoredMenu::totalScore).reversed()
                        .thenComparing(scoredMenu -> scoredMenu.menu().menuId()))
                .limit(input.candidateLimit())
                .toList();

        List<MenuRecommendationCandidateResult> candidates = toCandidates(scoredMenus);

        return new MenuRecommendationResult(type(), version(), candidates);
    }

    private ScoredMenu score(
            MenuRecommendationProfile menu,
            TasteProfileSnapshot participant,
            Map<Long, Long> selectedAttributeCategoryFrequency
    ) {
        long categoryMatchingCount = countMatches(
                menu.attributeCategoryIds(),
                participant.preferredAttributeCategoryIds()
        );
        long historyWeightMatchingCount = menu.attributeCategoryIds().stream()
                .mapToLong(categoryId -> selectedAttributeCategoryFrequency.getOrDefault(categoryId, 0L))
                .sum();

        double categoryMatchingScore = calculateCategoryMatchingScore(
                categoryMatchingCount,
                participant.preferredAttributeCategoryIds().size()
        );
        double historyWeightScore = calculateHistoryWeightScore(
                historyWeightMatchingCount,
                selectedAttributeCategoryFrequency
        );
        double totalScore = categoryMatchingScore + historyWeightScore;

        return new ScoredMenu(
                menu,
                categoryMatchingCount,
                historyWeightMatchingCount,
                categoryMatchingScore,
                historyWeightScore,
                totalScore
        );
    }

    private List<MenuRecommendationCandidateResult> toCandidates(List<ScoredMenu> scoredMenus) {
        return java.util.stream.IntStream.range(0, scoredMenus.size())
                .mapToObj(index -> {
                    ScoredMenu scoredMenu = scoredMenus.get(index);

                    return new MenuRecommendationCandidateResult(
                            scoredMenu.menu().menuId(),
                            index + 1,
                            scoredMenu.totalScore(),
                            Map.of(
                                    "categoryMatchingCount", scoredMenu.categoryMatchingCount(),
                                    "historyWeightMatchingCount", scoredMenu.historyWeightMatchingCount(),
                                    "categoryMatchingScore", scoredMenu.categoryMatchingScore(),
                                    "historyWeightScore", scoredMenu.historyWeightScore()
                            ),
                            Map.of()
                    );
                })
                .toList();
    }

    private double calculateCategoryMatchingScore(long matchingCount, int preferredCategoryCount) {
        if (preferredCategoryCount == 0) {
            return 0;
        }

        return matchingCount * (50.0 / preferredCategoryCount);
    }

    private double calculateHistoryWeightScore(
            long historyWeightMatchingCount,
            Map<Long, Long> selectedAttributeCategoryFrequency
    ) {
        long maxFrequency = selectedAttributeCategoryFrequency.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        if (maxFrequency == 0) {
            return 0;
        }

        return historyWeightMatchingCount * (50.0 / maxFrequency);
    }

    private long countMatches(List<Long> sourceIds, List<Long> targetIds) {
        Set<Long> targetIdSet = new HashSet<>(targetIds);

        return sourceIds.stream()
                .filter(targetIdSet::contains)
                .count();
    }

    private boolean containsAny(List<Long> sourceIds, List<Long> targetIds) {
        Set<Long> targetIdSet = new HashSet<>(targetIds);

        return sourceIds.stream()
                .anyMatch(targetIdSet::contains);
    }

    private record ScoredMenu(
            MenuRecommendationProfile menu,
            long categoryMatchingCount,
            long historyWeightMatchingCount,
            double categoryMatchingScore,
            double historyWeightScore,
            double totalScore
    ) {
    }
}
