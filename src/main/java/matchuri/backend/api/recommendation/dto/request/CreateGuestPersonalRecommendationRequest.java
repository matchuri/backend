package matchuri.backend.api.recommendation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

public record CreateGuestPersonalRecommendationRequest(
        @Schema(description = "선호 attribute category ID 목록입니다.", example = "[101,102]")
        List<@Positive Long> attributeCategoryIds,

        @Schema(description = "제외할 restriction ingredient ID 목록입니다.", example = "[201]")
        List<@Positive Long> restrictionIngredientIds,

        @Schema(description = "제외할 disliked menu item ID 목록입니다.", example = "[301]")
        List<@Positive Long> dislikedMenuItemIds,

        @Schema(
                description = "추천 요청 컨텍스트입니다. MVP에서는 선택 입력이며 mealTime, partySize, budgetLevel 같은 느슨한 JSON 값으로 시작합니다.",
                example = "{\"mealTime\":\"LUNCH\"}"
        )
        Map<String, Object> contextJson
) {
}
