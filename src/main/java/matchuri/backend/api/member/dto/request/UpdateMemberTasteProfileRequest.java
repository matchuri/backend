package matchuri.backend.api.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateMemberTasteProfileRequest(
        @Schema(
                description = "현재 선택한 attribute category ID 목록입니다. 전체 교체형 저장이므로 비우려면 빈 배열을 보내야 합니다.",
                example = "[1, 2]"
        )
        @NotNull(message = "attributeCategoryIds는 null일 수 없습니다.")
        List<@NotNull(message = "attributeCategoryIds 항목은 null일 수 없습니다.") Long> attributeCategoryIds,

        @Schema(
                description = "현재 선택한 restriction ingredient ID 목록입니다. 전체 교체형 저장이므로 비우려면 빈 배열을 보내야 합니다.",
                example = "[101]"
        )
        @NotNull(message = "restrictionIngredientIds는 null일 수 없습니다.")
        List<@NotNull(message = "restrictionIngredientIds 항목은 null일 수 없습니다.") Long> restrictionIngredientIds
) {
}
