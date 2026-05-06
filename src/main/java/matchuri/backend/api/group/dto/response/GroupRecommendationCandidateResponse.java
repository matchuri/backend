package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GroupRecommendationCandidateResponse(
        @Schema(description = "그룹 추천 후보 ID입니다.", example = "8001")
        Long candidateId,

        @Schema(description = "후보 메뉴 ID입니다.", example = "1001")
        Long menuId,

        @Schema(description = "후보 메뉴명입니다.", example = "비빔밥")
        String menuName,

        @Schema(description = "추천 순위입니다.", example = "1")
        Integer rankNo,

        @Schema(description = "Mock 추천 점수입니다.", example = "91.5")
        Double score,

        @Schema(description = "그룹 추천 근거 요약입니다.", example = "매운맛 선호와 제한 재료 회피 조건을 함께 만족합니다.")
        String reasonSummary,

        @Schema(description = "현재 찬성 투표 수입니다.", example = "3")
        Integer voteCount
) {
    public static GroupRecommendationCandidateResponse mockBibimbap() {
        return new GroupRecommendationCandidateResponse(
                8001L,
                1001L,
                "비빔밥",
                1,
                91.5,
                "매운맛 선호와 제한 재료 회피 조건을 함께 만족합니다.",
                3
        );
    }

    public static GroupRecommendationCandidateResponse mockPorkCutlet() {
        return new GroupRecommendationCandidateResponse(
                8002L,
                1002L,
                "돈까스",
                2,
                84.0,
                "바삭한 식감 선호자가 많고 대중적으로 합의하기 쉬운 후보입니다.",
                1
        );
    }

    public static GroupRecommendationCandidateResponse mockRiceNoodle() {
        return new GroupRecommendationCandidateResponse(
                8003L,
                1003L,
                "쌀국수",
                3,
                79.5,
                "따뜻한 국물 선호와 가벼운 점심 요구를 반영한 후보입니다.",
                0
        );
    }
}
