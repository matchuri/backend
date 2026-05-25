package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record GroupRecommendationSummaryResponse(
        @Schema(description = "그룹 추천 ID입니다. API 경로에서는 sessionId로 표현합니다.", example = "5001")
        Long sessionId,

        @Schema(description = "그룹 추천 상태입니다.", example = "FINALIZED")
        GroupRecommendationStatus status,

        @Schema(description = "추천 시작 시각입니다.", example = "2026-05-26T12:00:00")
        LocalDateTime startedAt,

        @Schema(description = "추천 종료 시각입니다. OPEN이면 null입니다.", example = "2026-05-26T12:15:00")
        LocalDateTime endedAt
) {
}
