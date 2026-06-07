package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GroupRecommendationCandidateResponse(
        @Schema(description = "그룹 추천 후보 ID입니다.", example = "8001")
        Long candidateId,

        @Schema(description = "후보 메뉴 ID입니다.", example = "1001")
        Long menuId,

        @Schema(description = "후보 메뉴명입니다.", example = "비빔밥")
        String menuName,

        @Schema(description = "후보 메뉴 이미지 URL입니다. 이미지가 없으면 null입니다.", example = "https://asset.matchuri.com/menu-items/1001/sample.jpg", nullable = true)
        String thumbnailUrl,

        @Schema(description = "추천 순위입니다.", example = "1")
        Integer rankNo,

        @Schema(description = "0에서 100 사이로 정규화된 추천 점수입니다.", example = "91.5")
        Double score,

        @Schema(description = "현재 찬성 투표 수입니다.", example = "3")
        Integer voteCount
) {
    public static GroupRecommendationCandidateResponse mockBibimbap() {
        return new GroupRecommendationCandidateResponse(
                8001L,
                1001L,
                "비빔밥",
                null,
                1,
                91.5,
                3
        );
    }

    public static GroupRecommendationCandidateResponse mockPorkCutlet() {
        return new GroupRecommendationCandidateResponse(
                8002L,
                1002L,
                "돈까스",
                null,
                2,
                84.0,
                1
        );
    }

    public static GroupRecommendationCandidateResponse mockRiceNoodle() {
        return new GroupRecommendationCandidateResponse(
                8003L,
                1003L,
                "쌀국수",
                null,
                3,
                79.5,
                0
        );
    }
}
