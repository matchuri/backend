package matchuri.backend.domain.recommendation.algorithm.group;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import matchuri.backend.domain.recommendation.algorithm.MenuRecommendationAlgorithm;
import matchuri.backend.domain.recommendation.algorithm.RecommendationAlgorithmType;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationInput;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationProfile;
import matchuri.backend.domain.recommendation.algorithm.input.TasteProfileSnapshot;
import matchuri.backend.domain.recommendation.algorithm.output.MenuRecommendationCandidateResult;
import matchuri.backend.domain.recommendation.algorithm.output.MenuRecommendationResult;
import matchuri.backend.domain.recommendation.algorithm.support.RecommendationScoreNormalizer;
import org.springframework.stereotype.Component;

@Component
public class GroupMenuRecommendationAlgorithmV1 implements MenuRecommendationAlgorithm {

    private static final String VERSION = "v1";
    private static final double PARTICIPANT_PREFERENCE_SCORE = 50.0;
    private static final double DISLIKED_MENU_PENALTY = 25.0;

    @Override
    public RecommendationAlgorithmType type() {
        return RecommendationAlgorithmType.GROUP;
    }

    @Override
    public String version() {
        return VERSION;
    }

    @Override
    public MenuRecommendationResult recommend(MenuRecommendationInput input) {
        Set<Long> restrictedIngredientIds = collectRestrictionIngredientIds(input.participants());

        List<ScoredMenu> scoredMenus = input.menus().stream()
                .filter(menu -> !containsAny(menu.ingredientIds(), restrictedIngredientIds))
                .filter(menu -> !input.recentlySkippedMenuIds().contains(menu.menuId()))
                .map(menu -> score(menu, input.participants()))
                .sorted(Comparator.comparing(ScoredMenu::totalScore).reversed()
                        .thenComparing(scoredMenu -> scoredMenu.menu().menuId()))
                .limit(input.candidateLimit())
                .toList();

        return new MenuRecommendationResult(type(), version(), toCandidates(scoredMenus));
    }

    private ScoredMenu score(MenuRecommendationProfile menu, List<TasteProfileSnapshot> participants) {
        long preferenceMatchingCount = 0;
        double preferenceScore = 0;
        int dislikedMemberCount = 0;

        for (TasteProfileSnapshot participant : participants) {
            long participantMatchingCount = countMatches(
                    menu.attributeCategoryIds(),
                    participant.preferredAttributeCategoryIds()
            );
            preferenceMatchingCount += participantMatchingCount;

            if (!participant.preferredAttributeCategoryIds().isEmpty()) {
                preferenceScore += participantMatchingCount
                        * (PARTICIPANT_PREFERENCE_SCORE / participant.preferredAttributeCategoryIds().size());
            }

            if (participant.dislikedMenuItemIds().contains(menu.menuId())) {
                dislikedMemberCount++;
            }
        }

        double dislikedPenalty = dislikedMemberCount * DISLIKED_MENU_PENALTY;
        double rawScore = preferenceScore - dislikedPenalty;
        double maxPossibleScore = participants.stream()
                .filter(participant -> !participant.preferredAttributeCategoryIds().isEmpty())
                .count() * PARTICIPANT_PREFERENCE_SCORE;

        return new ScoredMenu(
                menu,
                participants.size(),
                preferenceMatchingCount,
                dislikedMemberCount,
                preferenceScore,
                dislikedPenalty,
                RecommendationScoreNormalizer.normalize(rawScore, maxPossibleScore),
                rawScore
        );
    }

    private List<MenuRecommendationCandidateResult> toCandidates(List<ScoredMenu> scoredMenus) {
        return IntStream.range(0, scoredMenus.size())
                .mapToObj(index -> {
                    ScoredMenu scoredMenu = scoredMenus.get(index);

                    return new MenuRecommendationCandidateResult(
                            scoredMenu.menu().menuId(),
                            index + 1,
                            scoredMenu.normalizedScore(),
                            Map.of(
                                    "preferenceMatchingCount", scoredMenu.preferenceMatchingCount(),
                                    "dislikedMemberCount", scoredMenu.dislikedMemberCount(),
                                    "preferenceScore", scoredMenu.preferenceScore(),
                                    "dislikedPenalty", scoredMenu.dislikedPenalty(),
                                    "normalizedScore", scoredMenu.normalizedScore(),
                                    "rawScore", scoredMenu.totalScore()
                            ),
                            Map.of(
                                    "participantCount", scoredMenu.participantCount()
                            )
                    );
                })
                .toList();
    }

    private Set<Long> collectRestrictionIngredientIds(List<TasteProfileSnapshot> participants) {
        Set<Long> restrictedIngredientIds = new HashSet<>();

        for (TasteProfileSnapshot participant : participants) {
            restrictedIngredientIds.addAll(participant.restrictionIngredientIds());
        }

        return restrictedIngredientIds;
    }

    private long countMatches(List<Long> sourceIds, List<Long> targetIds) {
        Set<Long> targetIdSet = new HashSet<>(targetIds);

        return sourceIds.stream()
                .filter(targetIdSet::contains)
                .count();
    }

    private boolean containsAny(List<Long> sourceIds, Set<Long> targetIds) {
        return sourceIds.stream()
                .anyMatch(targetIds::contains);
    }

    private record ScoredMenu(
            MenuRecommendationProfile menu,
            int participantCount,
            long preferenceMatchingCount,
            int dislikedMemberCount,
            double preferenceScore,
            double dislikedPenalty,
            double normalizedScore,
            double totalScore
    ) {
    }
}
