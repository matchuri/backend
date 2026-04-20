package matchuri.backend.api.menu.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import matchuri.backend.domain.menu.entity.AttributeCategory;

public record UpdateAdminAttributeCategoryRequest(
        @Schema(
                description = "수정할 attribute category 이름입니다. null이면 이름을 변경하지 않습니다.",
                example = "순한맛",
                nullable = true,
                maxLength = AttributeCategory.NAME_MAX_LENGTH
        )
        @Pattern(regexp = "^(?!\\s*$).+", message = "name은 비어 있을 수 없습니다.")
        @Size(max = AttributeCategory.NAME_MAX_LENGTH, message = "name은 " + AttributeCategory.NAME_MAX_LENGTH + "자를 초과할 수 없습니다.")
        String name,

        @Schema(
                description = "수정할 정렬 순서입니다. null이면 정렬 순서를 변경하지 않습니다.",
                example = "20",
                nullable = true
        )
        Integer sortOrder,

        @Schema(
                description = "수정할 활성 여부입니다. null이면 활성 상태를 변경하지 않습니다.",
                example = "false",
                nullable = true
        )
        Boolean isActive
) {
}
