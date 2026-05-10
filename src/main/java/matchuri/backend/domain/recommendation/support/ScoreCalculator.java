package matchuri.backend.domain.recommendation.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import matchuri.backend.domain.menu.entity.AttributeCategory;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScoreCalculator {

    private static final int SCORE_FACTOR_COUNT = 2;
    private static final int RECOMMENDATION_CANDIDATE_LIMIT = 3;

    private final double categoryMatchingCountRate;
    private final double weightMatchingCountRate;

    public static ScoreCalculator of(List<AttributeCategory> preferAttributeCategories,
                                     Map<AttributeCategory, Long> categoryFrequencyMap) {

        double rawFieldRate = 100.0 / SCORE_FACTOR_COUNT;
        double fieldRate = Math.round(rawFieldRate * 100) / 100.0;

        int size = preferAttributeCategories.size();
        double categoryMatchingCountRate = fieldRate / size;

        Collection<Long> values = categoryFrequencyMap.values();
        long maxFrequency = Collections.max(values);
        double weightMatchingCountRate = fieldRate / maxFrequency;

        return new ScoreCalculator(categoryMatchingCountRate, weightMatchingCountRate);
    }

    public List<MenuItemScoreBoard> calculate(Map<Long, MenuItemScoreBoard> menuItemScoreBoardMap) {
        menuItemScoreBoardMap.values()
                .forEach(menuItemScoreBoard -> {
                    double categoryMatchingScore =
                            menuItemScoreBoard.getCategoryMatchingCount() * this.categoryMatchingCountRate;

                    double weightMatchingScore =
                            menuItemScoreBoard.getWeightMatchingCount() * this.weightMatchingCountRate;

                    double rawTotalScore = categoryMatchingScore + weightMatchingScore;
                    menuItemScoreBoard.setTotalScore(rawTotalScore);
                });

        return menuItemScoreBoardMap.values().stream()
                .sorted(Comparator.comparing(MenuItemScoreBoard::getTotalScore).reversed())
                .limit(RECOMMENDATION_CANDIDATE_LIMIT)
                .toList();
    }

}
