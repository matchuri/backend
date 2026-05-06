package matchuri.backend.api.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record CreateGroupRecommendationRequest(
        @Schema(
                description = "그룹 추천 컨텍스트입니다. MVP에서는 mealTime, partySize, locationLabel 같은 느슨한 JSON 값으로 시작합니다.",
                example = "{\"mealTime\":\"LUNCH\",\"partySize\":4}"
        )
        @NotNull(message = "contextJson은 null일 수 없습니다.")
        Map<String, Object> contextJson
) {
}
