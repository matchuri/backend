package matchuri.backend.api.menu.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MenuItemSummaryResponse(
        @Schema(description = "메뉴 ID입니다.", example = "1001")
        Long id,

        @Schema(description = "메뉴 코드입니다.", example = "PORK_CUTLET")
        String code,

        @Schema(description = "메뉴 표시 이름입니다.", example = "돈까스")
        String name
) {
}
