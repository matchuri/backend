package matchuri.backend.domain.member.command;

import java.util.List;

public record UpdateMemberTasteProfileCommand(
        List<Long> attributeCategoryIds,
        List<Long> restrictionIngredientIds,
        List<Long> dislikedMenuItemIds
) {
}
