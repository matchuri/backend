package matchuri.backend.api.group.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.group.dto.response.FinalizeGroupRecommendationResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "그룹 추천 최종 메뉴 확정 API의 공통 응답 envelope입니다.")
public record FinalizeGroupRecommendationApiResponse(
        boolean success,
        FinalizeGroupRecommendationResponse data,
        ErrorResponse error
) {
}
