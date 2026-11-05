package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record GroupRecommendationReadinessResponse(
        @Schema(description = "그룹 추천 ID입니다. API 경로에서는 sessionId로 표현합니다.", example = "5001")
        Long sessionId,

        @Schema(description = "그룹 추천 상태입니다.", example = "PREPARING")
        GroupRecommendationStatus status,

        @Schema(description = "그룹 추천 준비 진행 상태입니다.")
        GroupRecommendationReadinessProgressResponse progress,

        @Schema(description = "현재 활성 그룹 멤버별 준비 상태입니다.")
        List<GroupRecommendationReadinessMemberResponse> members
) {
}
