package matchuri.backend.api.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PersonalRecommendationCandidateListResponse(
        @Schema(description = "개인 추천 요청 ID입니다.", example = "9001")
        Long requestId,

        @Schema(description = "추천 후보 목록입니다.")
        List<PersonalRecommendationCandidateResponse> candidates
) {
    public static PersonalRecommendationCandidateListResponse mock() {
        return new PersonalRecommendationCandidateListResponse(9001L, PersonalRecommendationMocks.candidates());
    }
}
