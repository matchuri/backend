package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record CreateGroupRecommendationResponse(
        @Schema(description = "그룹 추천 ID입니다. API 경로에서는 sessionId로 표현합니다.", example = "5001")
        Long sessionId,

        @Schema(description = "그룹 추천 상태입니다.", example = "OPEN")
        GroupRecommendationStatus status,

        @Schema(description = "생성된 추천 후보 목록입니다.")
        List<GroupRecommendationCandidateResponse> candidates
) {
    public static CreateGroupRecommendationResponse mockOpen() {
        return new CreateGroupRecommendationResponse(
                5001L,
                GroupRecommendationStatus.OPEN,
                GroupMocks.candidates()
        );
    }
}
