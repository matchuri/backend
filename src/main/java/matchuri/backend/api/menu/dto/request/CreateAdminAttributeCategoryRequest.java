package matchuri.backend.api.menu.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import matchuri.backend.domain.menu.entity.AttributeCategory;

public record CreateAdminAttributeCategoryRequest(
        @Schema(
                description = "attribute category의 상위 유형입니다. 현재 허용 값은 FLAVOR, COOKING_METHOD, FOOD_CATEGORY, TEXTURE, TEMPERATURE 입니다.",
                example = "FLAVOR"
        )
        @NotBlank(message = "categoryType은 비어 있을 수 없습니다.")
        String categoryType,

        @Schema(
                description = "생성할 attribute category 코드입니다.",
                example = "SPICY",
                maxLength = AttributeCategory.CODE_MAX_LENGTH
        )
        @NotBlank(message = "code는 비어 있을 수 없습니다.")
        @Size(max = AttributeCategory.CODE_MAX_LENGTH, message = "code는 " + AttributeCategory.CODE_MAX_LENGTH
                + "자를 초과할 수 없습니다.")
        String code,

        @Schema(
                description = "화면과 설명에 노출할 attribute category 이름입니다.",
                example = "매운맛",
                maxLength = AttributeCategory.NAME_MAX_LENGTH
        )
        @NotBlank(message = "name은 비어 있을 수 없습니다.")
        @Size(max = AttributeCategory.NAME_MAX_LENGTH, message = "name은 " + AttributeCategory.NAME_MAX_LENGTH
                + "자를 초과할 수 없습니다.")
        String name,

        @Schema(
                description = "관리 화면과 공개 조회에서 사용할 정렬 순서입니다.",
                example = "10"
        )
        @NotNull(message = "sortOrder는 필수입니다.")
        Integer sortOrder
) {
}
