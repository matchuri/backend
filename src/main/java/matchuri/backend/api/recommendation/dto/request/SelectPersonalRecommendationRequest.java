package matchuri.backend.api.recommendation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SelectPersonalRecommendationRequest(
        @Schema(description = "최종 선택할 개인 추천 후보 ID입니다.", example = "10001")
        @NotNull(message = "selectedCandidateId는 null일 수 없습니다.")
        @Positive(message = "selectedCandidateId는 양수여야 합니다.")
        Long selectedCandidateId
) {
}
