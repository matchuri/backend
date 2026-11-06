package matchuri.backend.api.recommendation.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationRequestResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "개인 추천 요청 생성 API의 공통 응답 envelope입니다.")
public record PersonalRecommendationRequestApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "생성된 개인 추천 요청과 후보 목록입니다.")
        PersonalRecommendationRequestResponse data,

        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
