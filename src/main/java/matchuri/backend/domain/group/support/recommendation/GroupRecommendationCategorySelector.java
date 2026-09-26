package matchuri.backend.domain.group.support.recommendation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import matchuri.backend.domain.group.entity.GroupRecommendationCategorySource;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationProfile;
import matchuri.backend.domain.recommendation.algorithm.input.TasteProfileSnapshot;
import matchuri.backend.domain.recommendation.algorithm.output.MenuRecommendationCandidateResult;
import org.springframework.stereotype.Component;

@Component
public class GroupRecommendationCategorySelector {

    private static final int CATEGORY_LIMIT = 5;

    public List<SelectedCategory> select(
            List<TasteProfileSnapshot> participants,
            List<MenuRecommendationCandidateResult> candidates,
            List<MenuRecommendationProfile> menus,
            Map<Long, AttributeCategory> activeCategoriesById
    ) {
        Map<Long, MenuRecommendationProfile> menusById = new HashMap<>();
        menus.forEach(menu -> menusById.put(menu.menuId(), menu));
        List<MenuRecommendationCandidateResult> rankedCandidates = candidates.stream()
                .sorted(Comparator.comparingInt(MenuRecommendationCandidateResult::rankNo))
                .toList();
        Set<Long> candidateCategoryIds = new HashSet<>();
        for (MenuRecommendationCandidateResult candidate : rankedCandidates) {
            candidateCategoryIds.addAll(menusById.get(candidate.menuId()).attributeCategoryIds());
        }

        Set<Long> commonIds = new HashSet<>();
        if (!participants.isEmpty()) {
            commonIds.addAll(participants.getFirst().preferredAttributeCategoryIds());
            for (TasteProfileSnapshot participant : participants) {
                commonIds.retainAll(participant.preferredAttributeCategoryIds());
            }
        }
        commonIds.retainAll(candidateCategoryIds);
        commonIds.retainAll(activeCategoriesById.keySet());

        Map<Long, Integer> matchingCandidateCountByCategoryId = new HashMap<>();
        for (MenuRecommendationCandidateResult candidate : rankedCandidates) {
            Set<Long> menuCategoryIds = new HashSet<>(menusById.get(candidate.menuId()).attributeCategoryIds());
            for (Long categoryId : commonIds) {
                if (menuCategoryIds.contains(categoryId)) {
                    matchingCandidateCountByCategoryId.merge(categoryId, 1, Integer::sum);
                }
            }
        }

        Comparator<Long> categoryOrder = Comparator
                .comparing((Long id) -> activeCategoriesById.get(id).getCategoryType())
                .thenComparingInt(id -> activeCategoriesById.get(id).getSortOrder())
                .thenComparingLong(Long::longValue);
        LinkedHashSet<Long> selectedIds = new LinkedHashSet<>();
        List<SelectedCategory> selected = new ArrayList<>();
        commonIds.stream()
                .sorted(Comparator.comparingInt((Long id) -> matchingCandidateCountByCategoryId.getOrDefault(id, 0))
                        .reversed().thenComparing(categoryOrder))
                .limit(CATEGORY_LIMIT)
                .forEach(id -> {
                    selectedIds.add(id);
                    selected.add(new SelectedCategory(id, GroupRecommendationCategorySource.COMMON));
                });

        for (MenuRecommendationCandidateResult candidate : rankedCandidates) {
            if (selected.size() == CATEGORY_LIMIT) {
                break;
            }
            menusById.get(candidate.menuId()).attributeCategoryIds().stream()
                    .filter(activeCategoriesById::containsKey)
                    .sorted(categoryOrder)
                    .forEach(id -> {
                        if (selected.size() < CATEGORY_LIMIT && selectedIds.add(id)) {
                            selected.add(new SelectedCategory(id, GroupRecommendationCategorySource.MENU));
                        }
                    });
        }
        return List.copyOf(selected);
    }

    public record SelectedCategory(Long categoryId, GroupRecommendationCategorySource source) {
    }
}
