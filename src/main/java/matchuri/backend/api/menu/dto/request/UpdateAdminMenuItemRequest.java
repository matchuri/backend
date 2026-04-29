package matchuri.backend.api.menu.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import matchuri.backend.domain.menu.entity.MenuItem;

public record UpdateAdminMenuItemRequest(
        @Schema(
                description = "수정할 메뉴명입니다. null이면 이름을 변경하지 않습니다.",
                example = "새 돈까스",
                nullable = true,
                maxLength = MenuItem.NAME_MAX_LENGTH
        )
        @Pattern(regexp = "^(?!\\s*$).+", message = "name은 비어 있을 수 없습니다.")
        @Size(max = MenuItem.NAME_MAX_LENGTH, message = "name은 " + MenuItem.NAME_MAX_LENGTH + "자를 초과할 수 없습니다.")
        String name,

        @Schema(
                description = "수정할 메뉴 설명입니다. null이면 설명을 변경하지 않습니다.",
                example = "돼지고기를 튀겨 소스와 함께 먹는 바삭한 메뉴입니다.",
                nullable = true,
                maxLength = MenuItem.DESCRIPTION_MAX_LENGTH
        )
        @Size(
                max = MenuItem.DESCRIPTION_MAX_LENGTH,
                message = "description은 " + MenuItem.DESCRIPTION_MAX_LENGTH + "자를 초과할 수 없습니다."
        )
        String description,

        @Schema(
                description = "수정할 활성 여부입니다. null이면 활성 상태를 변경하지 않습니다.",
                example = "false",
                nullable = true
        )
        Boolean isActive
) {
}
