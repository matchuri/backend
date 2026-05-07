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
    public double categoryMatchingCountRate;
    public double weightMatchingCountRate;

    public static ScoreCalculator of(List<AttributeCategory> preferAttributeCategories,
                                     Map<AttributeCategory, Long> categoryFrequencyMap) {

        int fieldCount = ScoreCalculator.class.getDeclaredFields().length;
        double rawFieldRate = 100.0 / fieldCount;
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
                .limit(3)
                .toList();
    }

}
