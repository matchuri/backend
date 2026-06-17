package matchuri.backend.api.group.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.group.dto.response.CancelGroupRecommendationResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "그룹 추천 준비 세션 취소 API의 공통 응답 envelope입니다.")
public record CancelGroupRecommendationApiResponse(
        boolean success,
        CancelGroupRecommendationResponse data,
        ErrorResponse error
) {
}
