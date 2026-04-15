package matchuri.backend.api.member.dto.response;

import java.util.List;

public record MemberTasteProfileSummaryResponse(
        List<Object> attributeCategories,
        List<Object> restrictionIngredients,
        String profileVersion
) {
}
