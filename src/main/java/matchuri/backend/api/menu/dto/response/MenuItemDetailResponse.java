package matchuri.backend.api.menu.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record MenuItemDetailResponse(
        @Schema(description = "메뉴 ID입니다.", example = "1001")
        Long id,

        @Schema(description = "메뉴 코드입니다.", example = "PORK_CUTLET")
        String code,

        @Schema(description = "메뉴 표시 이름입니다.", example = "돈까스")
        String name,

        @Schema(description = "메뉴 설명입니다.", example = "돼지고기를 튀겨 소스와 함께 먹는 바삭한 메뉴입니다.")
        String description,

        @Schema(description = "메뉴에 연결된 활성 attribute category 목록입니다.")
        List<AttributeCategoryResponse> attributeCategories,

        @Schema(description = "메뉴에 연결된 활성 ingredient 목록입니다.")
        List<RestrictionIngredientResponse> ingredients
) {
}
