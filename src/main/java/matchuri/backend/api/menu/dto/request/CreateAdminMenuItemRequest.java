package matchuri.backend.api.menu.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import matchuri.backend.domain.menu.entity.MenuItem;

public record CreateAdminMenuItemRequest(
        @Schema(description = "메뉴 코드입니다. 생성 이후 수정하지 않는 비즈니스 키입니다.", example = "KIMCHI_FRIED_RICE")
        @NotBlank(message = "code는 필수입니다.")
        @Size(max = 50, message = "code는 최대 50자입니다.")
        String code,

        @Schema(description = "메뉴 표시 이름입니다.", example = "김치볶음밥")
        @NotBlank(message = "name은 필수입니다.")
        @Size(max = MenuItem.NAME_MAX_LENGTH, message = "name은 최대 120자입니다.")
        String name,

        @Schema(description = "메뉴 설명입니다.", example = "김치와 밥을 볶은 한식 메뉴입니다.")
        @Size(max = MenuItem.DESCRIPTION_MAX_LENGTH, message = "description은 최대 500자입니다.")
        String description,

        @Schema(description = "메뉴에 연결할 활성 attribute category ID 목록입니다.")
        @NotNull(message = "attributeCategoryIds는 필수입니다.")
        List<@NotNull(message = "attributeCategoryIds 항목은 null일 수 없습니다.") Long> attributeCategoryIds,

        @Schema(description = "메뉴에 연결할 활성 ingredient ID 목록입니다.")
        @NotNull(message = "ingredientIds는 필수입니다.")
        List<@NotNull(message = "ingredientIds 항목은 null일 수 없습니다.") Long> ingredientIds
) {
}
