package matchuri.backend.api.menu.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AdminMenuItemDetailResponse(
        @Schema(description = "메뉴 ID입니다.", example = "1001")
        Long id,

        @Schema(description = "메뉴 코드입니다.", example = "PORK_CUTLET")
        String code,

        @Schema(description = "메뉴 표시 이름입니다.", example = "돈까스")
        String name,

        @Schema(description = "메뉴 설명입니다.", example = "돼지고기를 튀겨 소스와 함께 먹는 바삭한 메뉴입니다.")
        String description,

        @Schema(description = "운영 기준 활성 여부입니다.", example = "true")
        boolean isActive,

        @Schema(description = "대표 이미지 공개 URL입니다. 이미지가 없으면 null입니다.")
        String thumbnailUrl,

        @Schema(description = "메뉴에 연결된 attribute category 목록입니다.")
        List<AdminAttributeCategoryResponse> attributeCategories,

        @Schema(description = "메뉴에 연결된 ingredient 목록입니다.")
        List<AdminIngredientResponse> ingredients
) {
}
