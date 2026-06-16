package matchuri.backend.api.menu.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateAdminMenuItemReferencesRequest(
        @Schema(description = "메뉴에 연결할 활성 attribute category ID 목록입니다. 빈 배열이면 연결을 모두 제거합니다.")
        @NotNull(message = "attributeCategoryIds는 필수입니다.")
        List<@NotNull(message = "attributeCategoryIds 항목은 null일 수 없습니다.") Long> attributeCategoryIds,

        @Schema(description = "메뉴에 연결할 활성 ingredient ID 목록입니다. 빈 배열이면 연결을 모두 제거합니다.")
        @NotNull(message = "ingredientIds는 필수입니다.")
        List<@NotNull(message = "ingredientIds 항목은 null일 수 없습니다.") Long> ingredientIds
) {
}
