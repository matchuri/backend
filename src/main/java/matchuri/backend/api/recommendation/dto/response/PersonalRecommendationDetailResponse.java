package matchuri.backend.api.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;

public record PersonalRecommendationDetailResponse(
        @Schema(description = "개인 추천 요청 ID입니다.", example = "9001")
        Long id,

        @Schema(description = "개인 추천 lifecycle 상태입니다.", example = "OPEN")
        PersonalRecommendationStatus status,

        @Schema(description = "추천 종료 시각입니다. 아직 종료되지 않았다면 null입니다.", example = "2026-05-06T12:15:00")
        LocalDateTime closedAt,

        @Schema(
                description = "추천 당시 위치 등 컨텍스트 JSON 문자열입니다. 클라이언트가 필요 시 파싱합니다.",
                example = "{\"latitude\":37.498095,\"longitude\":127.027610,\"radiusMeters\":1000,\"address\":\"서울 강남구 테헤란로 123\"}",
                nullable = true
        )
        String contextJson,

        @Schema(description = "추천 후보 목록입니다.")
        List<PersonalRecommendationCandidateResponse> candidates,

        @Schema(description = "최종 선택된 후보 ID입니다. 아직 선택하지 않았다면 null입니다.", example = "10001")
        Long selectedCandidateId
) {
    public static PersonalRecommendationDetailResponse mockSelected() {
        return new PersonalRecommendationDetailResponse(
                9001L,
                PersonalRecommendationStatus.SELECTED,
                LocalDateTime.of(2026, 5, 6, 12, 15),
                PersonalRecommendationMocks.contextJson(),
                PersonalRecommendationMocks.candidates(),
                10001L
        );
    }
}
