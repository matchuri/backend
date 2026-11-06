package matchuri.backend.domain.recommendation.command;

import java.util.List;

public record GuestPersonalRecommendationCommand(
        List<Long> attributeCategoryIds,
        List<Long> restrictionIngredientIds,
        List<Long> dislikedMenuItemIds,
        String contextJson
) {
    public GuestPersonalRecommendationCommand {
        attributeCategoryIds = attributeCategoryIds == null ? List.of() : List.copyOf(attributeCategoryIds);
        restrictionIngredientIds = restrictionIngredientIds == null ? List.of() : List.copyOf(restrictionIngredientIds);
        dislikedMenuItemIds = dislikedMenuItemIds == null ? List.of() : List.copyOf(dislikedMenuItemIds);
        contextJson = contextJson == null ? "{}" : contextJson;
    }
}
