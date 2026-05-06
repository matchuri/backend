package matchuri.backend.api.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;

public record PersonalRecommendationRequestResponse(
        @Schema(description = "개인 추천 요청 ID입니다.", example = "9001")
        Long requestId,

        @Schema(description = "개인 추천 처리 상태입니다.", example = "COMPLETED")
        PersonalRecommendationStatus status,

        @Schema(description = "추천 요청 시각입니다.", example = "2026-05-06T12:10:00")
        LocalDateTime requestedAt,

        @Schema(description = "추천 후보 목록입니다.")
        List<PersonalRecommendationCandidateResponse> candidates,

        @Schema(description = "추천 결과 요약 JSON입니다.")
        Map<String, Object> resultJson
) {
    public static PersonalRecommendationRequestResponse mockCompleted() {
        return new PersonalRecommendationRequestResponse(
                9001L,
                PersonalRecommendationStatus.COMPLETED,
                LocalDateTime.of(2026, 5, 6, 12, 10),
                PersonalRecommendationMocks.candidates(),
                PersonalRecommendationMocks.resultJson()
        );
    }
}
