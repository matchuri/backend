package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record CancelGroupRecommendationResponse(
        @Schema(description = "그룹 추천 ID입니다. API 경로에서는 sessionId로 표현합니다.", example = "5001")
        Long sessionId,

        @Schema(description = "취소 후 그룹 추천 상태입니다.", example = "CANCELED")
        GroupRecommendationStatus status,

        @Schema(description = "취소 처리 시각입니다.", example = "2026-05-06T12:10:00")
        LocalDateTime canceledAt
) {
}
