package matchuri.backend.api.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberTasteDislikedMenuItemResponse(
        @Schema(description = "선택된 disliked menu item ID입니다.", example = "1001")
        Long id,

        @Schema(description = "선택된 disliked menu item 코드입니다.", example = "PORK_CUTLET")
        String code,

        @Schema(description = "선택된 disliked menu item 표시 이름입니다.", example = "돈까스")
        String name
) {
}
