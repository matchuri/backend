package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record ReadyGroupRecommendationResponse(
        @Schema(description = "그룹 추천 ID입니다. API 경로에서는 sessionId로 표현합니다.", example = "5001")
        Long sessionId,

        @Schema(description = "그룹 추천 상태입니다. 전원 준비 완료 전에는 PREPARING, 전원 준비 완료 후에는 OPEN입니다.", example = "PREPARING")
        GroupRecommendationStatus status,

        @Schema(description = "그룹 추천 준비 진행 상태입니다.")
        GroupRecommendationReadinessProgressResponse readiness,

        @Schema(description = "전원 준비 완료로 OPEN 전환된 경우 생성된 추천 후보 목록입니다. 아직 PREPARING이면 빈 목록입니다.")
        List<GroupRecommendationCandidateResponse> candidates
) {
}
