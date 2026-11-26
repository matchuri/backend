package matchuri.backend.api.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PersonalRecommendationCandidateResponse(
        @Schema(description = "개인 추천 후보 ID입니다.", example = "10001")
        Long id,

        @Schema(description = "추천된 메뉴 ID입니다.", example = "1001")
        Long menuId,

        @Schema(description = "추천된 메뉴명입니다.", example = "비빔밥")
        String menuName,

        @Schema(description = "추천 후보 메뉴 이미지 URL입니다. 이미지가 없으면 null입니다.", example = "https://asset.matchuri.com/menu-items/1001/sample.jpg", nullable = true)
        String thumbnailUrl,

        @Schema(description = """
                추천 순위입니다. 신규 회원 개인 추천 v1.4는 소수 첫째 자리까지 반올림한 응답 점수 내림차순입니다.
                1위 동점은 메뉴 ID 오름차순으로 정합니다. 2위부터는 동점 후보 중 앞서 선정한 메뉴와
                조리 방식이 겹치지 않는 후보를 먼저 고려하고, 이어 음식 분류의 겹침 여부와 메뉴 ID 오름차순으로 정합니다.
                기존 추천은 저장된 순위로 조회합니다.
                """, example = "1")
        Integer rankNo,

        @Schema(description = """
                취향과 선택 이력을 반영한 0~100 추천 점수입니다. 소수 첫째 자리까지 반올림합니다.
                회원 개인 추천은 유형마다 선택한 속성 ID 중 하나 이상이 메뉴와 일치하면 해당 유형을 충족합니다.
                선호가 있고 모든 선호 유형을 충족하면 선택 이력과 관계없이 100점입니다.
                부분 충족은 남은 점수의 최대 절반을 이력 보너스로 더합니다. 선호가 없으면 이력 일치 비중만 사용합니다.
                최종 점수를 정렬·저장·응답에 사용합니다. 기존 추천은 저장된 점수로 조회합니다.
                만족 확률을 의미하지 않습니다.
                """, example = "93.5")
        Double score
) {
    public static PersonalRecommendationCandidateResponse mockBibimbap() {
        return new PersonalRecommendationCandidateResponse(
                10001L,
                1001L,
                "비빔밥",
                null,
                1,
                93.5
        );
    }

    public static PersonalRecommendationCandidateResponse mockPorkCutlet() {
        return new PersonalRecommendationCandidateResponse(
                10002L,
                1002L,
                "돈까스",
                null,
                2,
                86.0
        );
    }

    public static PersonalRecommendationCandidateResponse mockRiceNoodle() {
        return new PersonalRecommendationCandidateResponse(
                10003L,
                1003L,
                "쌀국수",
                null,
                3,
                81.5
        );
    }
}
