package matchuri.backend.api.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.domain.menu.entity.CategoryType;

public record MemberTasteAttributeCategoryResponse(
        @Schema(description = "선택된 attribute category ID입니다.", example = "1")
        Long id,

        @Schema(description = "선택된 attribute category의 상위 유형입니다.", example = "FLAVOR")
        CategoryType categoryType,

        @Schema(description = "선택된 attribute category 코드입니다.", example = "SPICY")
        String code,

        @Schema(description = "선택된 attribute category 표시 이름입니다.", example = "매운맛")
        String name,

        @Schema(description = "화면 표시 순서를 위한 정렬 값입니다.", example = "10")
        int sortOrder
) {
}
