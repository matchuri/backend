package matchuri.backend.api.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record VoteGroupRecommendationRequest(
        @Schema(description = "투표할 그룹 추천 후보 ID입니다.", example = "8001")
        @NotNull(message = "candidateId는 null일 수 없습니다.")
        @Positive(message = "candidateId는 양수여야 합니다.")
        Long candidateId,

        @Schema(description = "투표 값입니다. MVP에서는 1=찬성, 0=비선호로 사용합니다.", example = "1")
        @NotNull(message = "voteValue는 null일 수 없습니다.")
        @Min(value = 0, message = "voteValue는 0 또는 1이어야 합니다.")
        @Max(value = 1, message = "voteValue는 0 또는 1이어야 합니다.")
        Integer voteValue
) {
}
