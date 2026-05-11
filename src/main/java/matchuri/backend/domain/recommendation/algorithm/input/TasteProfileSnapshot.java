package matchuri.backend.domain.recommendation.algorithm.input;

import java.util.List;

public record TasteProfileSnapshot(
        Long memberId,
        String participantKey,
        List<Long> preferredAttributeCategoryIds,
        List<Long> restrictionIngredientIds,
        List<Long> dislikedMenuItemIds
) {
    public TasteProfileSnapshot {
        preferredAttributeCategoryIds = preferredAttributeCategoryIds == null
                ? List.of()
                : List.copyOf(preferredAttributeCategoryIds);
        restrictionIngredientIds = restrictionIngredientIds == null
                ? List.of()
                : List.copyOf(restrictionIngredientIds);
        dislikedMenuItemIds = dislikedMenuItemIds == null
                ? List.of()
                : List.copyOf(dislikedMenuItemIds);
    }
}
