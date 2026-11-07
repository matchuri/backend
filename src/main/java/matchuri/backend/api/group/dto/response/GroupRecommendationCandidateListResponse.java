package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record GroupRecommendationCandidateListResponse(
        @Schema(description = "그룹 추천 ID입니다. API 경로에서는 sessionId로 표현합니다.", example = "5001")
        Long sessionId,

        @Schema(description = "추천 후보 목록입니다.")
        List<GroupRecommendationCandidateResponse> candidates
) {
    public static GroupRecommendationCandidateListResponse mock() {
        return new GroupRecommendationCandidateListResponse(5001L, GroupMocks.candidates());
    }
}
