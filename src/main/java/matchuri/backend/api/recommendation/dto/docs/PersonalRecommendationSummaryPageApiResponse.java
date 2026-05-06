package matchuri.backend.api.recommendation.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationResponse;
import matchuri.backend.global.api.ErrorResponse;
import matchuri.backend.global.api.PageResponse;

@Schema(description = "개인 추천 이력 목록 API의 공통 응답 envelope입니다.")
public record PersonalRecommendationSummaryPageApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "성공 시 반환되는 개인 추천 이력 페이지입니다.")
        PageResponse<PersonalRecommendationResponse> data,

        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
