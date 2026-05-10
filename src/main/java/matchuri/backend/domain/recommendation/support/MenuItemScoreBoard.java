package matchuri.backend.domain.recommendation.support;

import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.MenuAttributeCategory;
import matchuri.backend.domain.menu.entity.MenuItem;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MenuItemScoreBoard {
    private MenuItem menuItem;
    private long categoryMatchingCount;
    private long weightMatchingCount;
    @Setter
    private double totalScore;

    /**
     * 메뉴별 추천 점수판을 초기 점수 상태로 생성한다.
     *
     * @param menuItem 점수를 계산할 메뉴
     * @return 초기화된 메뉴 점수판
     */
    public static MenuItemScoreBoard of(MenuItem menuItem) {
        return new MenuItemScoreBoard(menuItem, 0, 0, 0);
    }

    /**
     * 회원 선호 attribute category와 일치한 개수를 누적한다.
     *
     * @param count 추가할 일치 개수
     */
    public void addCategoryMatchingCount(long count) {
        categoryMatchingCount += count;
    }

    /**
     * 메뉴의 attribute category가 과거 선택 이력에서 등장한 빈도 합계를 반영한다.
     *
     * @param categoryFrequencyMap 과거 선택 메뉴의 attribute category 빈도 map
     */
    public void setWeightMatchingCount(Map<AttributeCategory, Long> categoryFrequencyMap) {
        List<AttributeCategory> categories = menuItem.getMenuAttributeCategories().stream()
                .map(MenuAttributeCategory::getAttributeCategory)
                .toList();

        this.weightMatchingCount = categories.stream()
                .mapToLong(category -> categoryFrequencyMap.getOrDefault(category, 0L))
                .sum();
    }

}
