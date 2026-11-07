package matchuri.backend.api.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record VoteGroupRecommendationRequest(
        @Schema(description = "투표할 그룹 추천 후보 ID입니다.", example = "8001")
        @NotNull(message = "candidateId는 null일 수 없습니다.")
        @Positive(message = "candidateId는 양수여야 합니다.")
        Long candidateId
) {
}
