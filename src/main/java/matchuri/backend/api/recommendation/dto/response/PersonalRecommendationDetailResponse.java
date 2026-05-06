package matchuri.backend.api.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;

public record PersonalRecommendationDetailResponse(
        @Schema(description = "개인 추천 요청 ID입니다.", example = "9001")
        Long id,

        @Schema(description = "개인 추천 처리 상태입니다.", example = "COMPLETED")
        PersonalRecommendationStatus status,

        @Schema(description = "요청 컨텍스트 JSON입니다.")
        Map<String, Object> contextJson,

        @Schema(description = "추천 후보 목록입니다.")
        List<PersonalRecommendationCandidateResponse> candidates,

        @Schema(description = "최종 선택된 후보 ID입니다. 아직 선택하지 않았다면 null입니다.", example = "10001")
        Long selectedCandidateId
) {
    public static PersonalRecommendationDetailResponse mockSelected() {
        return new PersonalRecommendationDetailResponse(
                9001L,
                PersonalRecommendationStatus.COMPLETED,
                PersonalRecommendationMocks.contextJson(),
                PersonalRecommendationMocks.candidates(),
                10001L
        );
    }
}
