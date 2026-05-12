package matchuri.backend.api.recommendation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

public record CreatePersonalRecommendationRequest(
        @Schema(
                description = "추천 요청 컨텍스트입니다. MVP에서는 선택 입력이며 mealTime, partySize, budgetLevel 같은 느슨한 JSON 값으로 시작합니다.",
                example = "{\"mealTime\":\"LUNCH\"}"
        )
        Map<String, Object> contextJson
) {
}
