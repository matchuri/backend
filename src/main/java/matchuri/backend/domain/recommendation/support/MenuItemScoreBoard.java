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

    public static MenuItemScoreBoard of(MenuItem menuItem) {
        return new MenuItemScoreBoard(menuItem, 0, 0, 0);
    }

    public void addCategoryMatchingCount(long count) {
        categoryMatchingCount += count;
    }

    public void setWeightMatchingCount(Map<AttributeCategory, Long> categoryFrequencyMap) {
        List<AttributeCategory> categories = menuItem.getMenuAttributeCategories().stream()
                .map(MenuAttributeCategory::getAttributeCategory)
                .toList();

        long weightMatchingCount = categories.stream()
                .mapToLong(category -> categoryFrequencyMap.getOrDefault(category, 0L))
                .sum();
    }

}
