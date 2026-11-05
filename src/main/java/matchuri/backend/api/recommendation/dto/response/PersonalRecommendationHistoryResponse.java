package matchuri.backend.api.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;

public record PersonalRecommendationHistoryResponse(
        @Schema(description = "개인 추천 ID입니다.", example = "9001")
        long id,

        @Schema(description = "개인 추천 lifecycle 상태입니다.", example = "OPEN")
        PersonalRecommendationStatus status,

        @Schema(description = "추천 실행 시각입니다.", example = "2026-05-06T12:10:00")
        LocalDateTime requestedAt,

        @Schema(description = "추천 종료 시각입니다. OPEN 상태이면 null입니다.", nullable = true)
        LocalDateTime closedAt,

        @Schema(description = "추천 생성 당시 저장된 대표 메뉴의 0~100 정규화 추천 점수입니다. 현재 취향이나 계산 규칙으로 다시 계산하지 않으며 만족 확률을 의미하지 않습니다.", example = "93.5", nullable = true)
        Double score,

        @Schema(description = "대표 메뉴명입니다.", example = "비빔밥", nullable = true)
        String menuName,

        @Schema(description = "대표 메뉴의 활성 attribute category 이름 목록입니다.", example = "[\"매운맛\", \"한식\"]")
        List<String> tags,

        @Schema(description = "대표 메뉴 이미지 URL입니다.", example = "https://asset.matchuri.com/menu-items/1001/sample.jpg", nullable = true)
        String thumbnailUrl
) {
}
