package matchuri.backend.api.recommendation.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.recommendation.dto.response.GuestPersonalRecommendationResponse;
import matchuri.backend.global.api.ErrorResponse;

public record GuestPersonalRecommendationApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "비회원 개인 추천 결과입니다.")
        GuestPersonalRecommendationResponse data,

        @Schema(description = "실패 시 오류 정보입니다.", nullable = true)
        ErrorResponse error
) {
    public static GuestPersonalRecommendationApiResponse mock() {
        return new GuestPersonalRecommendationApiResponse(true, GuestPersonalRecommendationResponse.mock(), null);
    }
}
