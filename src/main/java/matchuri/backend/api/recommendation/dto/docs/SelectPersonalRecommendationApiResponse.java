package matchuri.backend.api.recommendation.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.recommendation.dto.response.SelectPersonalRecommendationResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "개인 추천 후보 선택 API의 공통 응답 envelope입니다.")
public record SelectPersonalRecommendationApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "선택 반영 결과입니다.")
        SelectPersonalRecommendationResponse data,

        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
