package matchuri.backend.api.recommendation.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationHistoryResponse;
import matchuri.backend.global.api.ErrorResponse;
import matchuri.backend.global.api.PageResponse;

@Schema(description = "v2 개인 추천 이력 목록 API의 공통 응답 envelope입니다.")
public record PersonalRecommendationHistoryPageApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "성공 시 반환되는 개인 추천 이력 페이지입니다.")
        PageResponse<PersonalRecommendationHistoryResponse> data,

        @Schema(description = "실패 정보입니다. 성공 시 null입니다.", nullable = true)
        ErrorResponse error
) {
}
