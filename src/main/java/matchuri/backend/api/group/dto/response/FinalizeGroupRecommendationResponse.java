package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record FinalizeGroupRecommendationResponse(
        @Schema(description = "그룹 추천 ID입니다. API 경로에서는 sessionId로 표현합니다.", example = "5001")
        Long sessionId,

        @Schema(description = "확정 후 그룹 추천 상태입니다.", example = "FINALIZED")
        GroupRecommendationStatus status,

        @Schema(description = "최종 확정 후보입니다.")
        GroupRecommendationCandidateResponse finalCandidate,

        @Schema(description = "최종 확정 시각입니다.", example = "2026-05-06T12:25:00")
        LocalDateTime finalizedAt
) {
    public static FinalizeGroupRecommendationResponse mockFinalized() {
        return new FinalizeGroupRecommendationResponse(
                5001L,
                GroupRecommendationStatus.FINALIZED,
                GroupRecommendationCandidateResponse.mockBibimbap(),
                LocalDateTime.of(2026, 5, 6, 12, 25)
        );
    }
}
