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

    /**
     * 선호 카테고리 수와 과거 선택 카테고리 최대 빈도를 기준으로 점수 계산기를 생성한다.
     *
     * @param preferAttributeCategories 회원이 선호한 attribute category 목록
     * @param categoryFrequencyMap 과거 선택 메뉴의 attribute category 빈도 map
     * @return 점수 항목별 비율이 계산된 점수 계산기
     */
    public static ScoreCalculator of(List<AttributeCategory> preferAttributeCategories,
                                     Map<AttributeCategory, Long> categoryFrequencyMap) {

        double rawFieldRate = 100.0 / SCORE_FACTOR_COUNT;
        double fieldRate = Math.round(rawFieldRate * 100) / 100.0;

        int size = preferAttributeCategories.size();
        double categoryMatchingCountRate = size == 0 ? 0 : fieldRate / size;

        Collection<Long> values = categoryFrequencyMap.values();
        long maxFrequency = values.isEmpty() ? 0 : Collections.max(values);
        double weightMatchingCountRate = maxFrequency == 0 ? 0 : fieldRate / maxFrequency;

        return new ScoreCalculator(categoryMatchingCountRate, weightMatchingCountRate);
    }

    /**
     * 후보 메뉴 점수판에 최종 점수를 반영하고 상위 개인 추천 후보를 반환한다.
     *
     * @param menuItemScoreBoardMap 후보 메뉴별 점수판 map
     * @return 최종 점수 기준 상위 추천 후보 목록
     */
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
