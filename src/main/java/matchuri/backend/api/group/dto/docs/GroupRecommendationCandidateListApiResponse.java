package matchuri.backend.api.group.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.group.dto.response.GroupRecommendationCandidateListResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "그룹 추천 후보 목록 API의 공통 응답 envelope입니다.")
public record GroupRecommendationCandidateListApiResponse(
        boolean success,
        GroupRecommendationCandidateListResponse data,
        ErrorResponse error
) {
}
