package matchuri.backend.api.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record GuestPersonalRecommendationResponse(
        @Schema(description = "비회원 개인 추천 후보 목록입니다.")
        List<GuestPersonalRecommendationCandidateResponse> candidates
) {
    public static GuestPersonalRecommendationResponse mock() {
        return new GuestPersonalRecommendationResponse(List.of(
                GuestPersonalRecommendationCandidateResponse.mockBibimbap()
        ));
    }
}
