package matchuri.backend.api.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PersonalRecommendationCandidateResponse(
        @Schema(description = "개인 추천 후보 ID입니다.", example = "10001")
        Long id,

        @Schema(description = "추천된 메뉴 ID입니다.", example = "1001")
        Long menuId,

        @Schema(description = "추천된 메뉴명입니다.", example = "비빔밥")
        String menuName,

        @Schema(description = "추천 순위입니다.", example = "1")
        Integer rankNo,

        @Schema(description = "Mock 추천 점수입니다.", example = "93.5")
        Double score,

        @Schema(description = "사용자에게 보여줄 추천 근거 요약입니다.", example = "채소와 매운맛 선호가 잘 맞는 후보입니다.")
        String reasonSummary
) {
    public static PersonalRecommendationCandidateResponse mockBibimbap() {
        return new PersonalRecommendationCandidateResponse(
                10001L,
                1001L,
                "비빔밥",
                1,
                93.5,
                "채소와 매운맛 선호가 잘 맞는 후보입니다."
        );
    }

    public static PersonalRecommendationCandidateResponse mockPorkCutlet() {
        return new PersonalRecommendationCandidateResponse(
                10002L,
                1002L,
                "돈까스",
                2,
                86.0,
                "바삭한 식감 선호와 든든한 점심 상황에 맞는 후보입니다."
        );
    }

    public static PersonalRecommendationCandidateResponse mockRiceNoodle() {
        return new PersonalRecommendationCandidateResponse(
                10003L,
                1003L,
                "쌀국수",
                3,
                81.5,
                "따뜻한 국물과 부담 없는 식사를 원하는 상황에 맞는 후보입니다."
        );
    }
}
