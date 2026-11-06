package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import org.jspecify.annotations.Nullable;

public record GroupRecommendationV2SummaryResponse(
        @Schema(description = "그룹 추천 ID입니다. API 경로에서는 sessionId로 표현합니다.", example = "5001")
        Long sessionId,

        @Schema(description = "그룹 추천 상태입니다.", example = "FINALIZED")
        GroupRecommendationStatus status,

        @Schema(
                description = "최종 선정된 메뉴명입니다. FINALIZED가 아니거나 최종 후보가 없으면 null입니다.",
                example = "비빔밥",
                nullable = true
        )
        @Nullable String selectedMenuName,

        @Schema(description = "추천 세션 생성 시각입니다.", example = "2026-05-26T12:00:00")
        LocalDateTime createdAt,

        @Schema(description = "투표 시작 시각입니다. PREPARING이면 null입니다.", example = "2026-05-26T12:05:00", nullable = true)
        @Nullable LocalDateTime startedAt,

        @Schema(description = "추천 종료 시각입니다. PREPARING 또는 OPEN이면 null입니다.", example = "2026-05-26T12:15:00", nullable = true)
        @Nullable LocalDateTime endedAt
) {
}
