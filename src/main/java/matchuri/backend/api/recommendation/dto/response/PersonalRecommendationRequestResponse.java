package matchuri.backend.api.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCloseReason;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;

public record PersonalRecommendationRequestResponse(
        @Schema(description = "개인 추천 요청 ID입니다.", example = "9001")
        Long requestId,

        @Schema(description = "개인 추천 처리 상태입니다.", example = "COMPLETED")
        PersonalRecommendationStatus status,

        @Schema(description = "추천 요청 시각입니다.", example = "2026-05-06T12:10:00")
        LocalDateTime requestedAt,

        @Schema(description = "추천 종료 시각입니다. 아직 선택/재요청/만료로 종료되지 않았다면 null입니다.", example = "2026-05-06T12:15:00")
        LocalDateTime closedAt,

        @Schema(description = "추천 종료 사유입니다. 아직 종료되지 않았다면 null입니다.", example = "SELECTED")
        PersonalRecommendationCloseReason closeReason,

        @Schema(description = "추천 후보 목록입니다.")
        List<PersonalRecommendationCandidateResponse> candidates
) {
    public static PersonalRecommendationRequestResponse mockCompleted() {
        return new PersonalRecommendationRequestResponse(
                9001L,
                PersonalRecommendationStatus.COMPLETED,
                LocalDateTime.of(2026, 5, 6, 12, 10),
                null,
                null,
                PersonalRecommendationMocks.candidates()
        );
    }
}
