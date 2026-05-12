package matchuri.backend.api.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GuestPersonalRecommendationCandidateResponse(
        @Schema(description = "추천된 메뉴 ID입니다.", example = "1001")
        Long menuId,

        @Schema(description = "추천된 메뉴명입니다.", example = "비빔밥")
        String menuName,

        @Schema(description = "추천 순위입니다.", example = "1")
        Integer rankNo,

        @Schema(description = "추천 점수입니다.", example = "93.5")
        Double score
) {
    public static GuestPersonalRecommendationCandidateResponse mockBibimbap() {
        return new GuestPersonalRecommendationCandidateResponse(
                1001L,
                "비빔밥",
                1,
                93.5
        );
    }
}
