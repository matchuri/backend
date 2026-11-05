package matchuri.backend.api.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;

public record SelectPersonalRecommendationResponse(
        @Schema(description = "개인 추천 요청 ID입니다.", example = "9001")
        Long id,

        @Schema(description = "개인 추천 lifecycle 상태입니다.", example = "SELECTED")
        PersonalRecommendationStatus status,

        @Schema(description = "최종 선택 후보 ID입니다.", example = "10001")
        Long selectedCandidateId,

        @Schema(description = "추천 종료 시각입니다.", example = "2026-05-06T12:15:00")
        LocalDateTime closedAt,

        @Schema(description = "선택 반영 시각입니다.", example = "2026-05-06T12:15:00")
        LocalDateTime updatedAt
) {
    public static SelectPersonalRecommendationResponse mockSelected(Long selectedCandidateId) {
        return new SelectPersonalRecommendationResponse(
                9001L,
                PersonalRecommendationStatus.SELECTED,
                selectedCandidateId,
                LocalDateTime.of(2026, 5, 6, 12, 15),
                LocalDateTime.of(2026, 5, 6, 12, 15)
        );
    }
}
