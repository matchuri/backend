package matchuri.backend.api.recommendation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationRerollType;

public record RerollPersonalRecommendationRequest(
        @Schema(description = "재요청 타입입니다.", example = "NOT_SATISFIED")
        @NotNull(message = "재요청 타입은 필수입니다.")
        PersonalRecommendationRerollType rerollType,

        @Schema(description = "새 추천 요청 컨텍스트 JSON입니다.", example = "{\"mealTime\":\"LUNCH\"}")
        Map<String, Object> contextJson
) {
}
