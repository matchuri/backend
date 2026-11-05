package matchuri.backend.recommendation.algorithm.support;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import matchuri.backend.catalog.entity.CategoryType;

public final class RecommendationPreferenceMatchCalculator {

    private RecommendationPreferenceMatchCalculator() {
    }

    public static PreferenceMatch calculate(
            List<Long> menuAttributeIds,
            List<Long> preferredAttributeIds,
            Map<Long, CategoryType> categoryTypes
    ) {
        return calculate(menuAttributeIds, preferredAttributeIds, categoryTypes,
                EnumSet.of(CategoryType.FOOD_CATEGORY, CategoryType.TEMPERATURE));
    }

    public static PreferenceMatch calculateByCategoryType(
            List<Long> menuAttributeIds,
            List<Long> preferredAttributeIds,
            Map<Long, CategoryType> categoryTypes
    ) {
        return calculate(menuAttributeIds, preferredAttributeIds, categoryTypes, EnumSet.allOf(CategoryType.class));
    }

    private static PreferenceMatch calculate(
            List<Long> menuAttributeIds,
            List<Long> preferredAttributeIds,
            Map<Long, CategoryType> categoryTypes,
            Set<CategoryType> groupedTypes
    ) {
        Set<Long> preferredIds = new HashSet<>(preferredAttributeIds);
        Set<Long> matchingIds = new HashSet<>(menuAttributeIds);
        matchingIds.retainAll(preferredIds);

        return new PreferenceMatch(countUnits(matchingIds, categoryTypes, groupedTypes),
                countUnits(preferredIds, categoryTypes, groupedTypes));
    }

    private static int countUnits(
            Set<Long> categoryIds,
            Map<Long, CategoryType> categoryTypes,
            Set<CategoryType> groupedTypes
    ) {
        Set<CategoryType> presentTypes = EnumSet.noneOf(CategoryType.class);
        int individualCount = 0;

        for (Long categoryId : categoryIds) {
            CategoryType type = categoryTypes.get(categoryId);
            if (groupedTypes.contains(type)) {
                presentTypes.add(type);
            } else {
                individualCount++;
            }
        }

        return individualCount + presentTypes.size();
    }

    public record PreferenceMatch(long matchingCount, int preferredUnitCount) {
    }
}
