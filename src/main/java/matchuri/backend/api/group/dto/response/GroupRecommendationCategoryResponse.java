package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.domain.group.entity.GroupRecommendationCategorySource;
import matchuri.backend.domain.menu.entity.CategoryType;

public record GroupRecommendationCategoryResponse(
        @Schema(description = "속성 카테고리 ID입니다.", example = "101")
        Long id,

        @Schema(description = "속성 카테고리 유형입니다.", example = "FLAVOR")
        CategoryType categoryType,

        @Schema(description = "속성 카테고리 코드입니다.", example = "SPICY")
        String code,

        @Schema(description = "현재 카테고리 표시 이름입니다. 이름은 추천 시점에 스냅샷하지 않습니다.", example = "매운맛")
        String name,

        @Schema(description = "표시 순위입니다.", example = "1")
        int rankNo,

        @Schema(description = "COMMON은 당시 모든 그룹원의 공통 취향, MENU는 후보 메뉴에서 보충한 항목입니다.", example = "COMMON")
        GroupRecommendationCategorySource source
) {
}
