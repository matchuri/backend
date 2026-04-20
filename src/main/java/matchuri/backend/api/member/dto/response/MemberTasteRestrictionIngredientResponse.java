package matchuri.backend.api.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberTasteRestrictionIngredientResponse(
        @Schema(description = "선택된 restriction ingredient ID입니다.", example = "101")
        Long id,

        @Schema(description = "선택된 restriction ingredient 코드입니다.", example = "PEANUT")
        String code,

        @Schema(description = "선택된 restriction ingredient 표시 이름입니다.", example = "땅콩")
        String name,

        @Schema(description = "알레르기 유발 재료 여부입니다.", example = "true")
        boolean allergen,

        @Schema(description = "화면 표시 순서를 위한 정렬 값입니다.", example = "10")
        int sortOrder
) {
}
