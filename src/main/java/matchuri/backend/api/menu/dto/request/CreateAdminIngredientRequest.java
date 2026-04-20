package matchuri.backend.api.menu.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import matchuri.backend.domain.menu.entity.Ingredient;

public record CreateAdminIngredientRequest(
        @Schema(
                description = "생성할 ingredient 코드입니다.",
                example = "PEANUT",
                maxLength = Ingredient.CODE_MAX_LENGTH
        )
        @NotBlank(message = "code는 비어 있을 수 없습니다.")
        @Size(max = Ingredient.CODE_MAX_LENGTH, message = "code는 " + Ingredient.CODE_MAX_LENGTH + "자를 초과할 수 없습니다.")
        String code,

        @Schema(
                description = "화면과 설명에 노출할 ingredient 이름입니다.",
                example = "땅콩",
                maxLength = Ingredient.NAME_MAX_LENGTH
        )
        @NotBlank(message = "name은 비어 있을 수 없습니다.")
        @Size(max = Ingredient.NAME_MAX_LENGTH, message = "name은 " + Ingredient.NAME_MAX_LENGTH + "자를 초과할 수 없습니다.")
        String name,

        @Schema(
                description = "알레르기 유발 재료 여부입니다.",
                example = "true"
        )
        @NotNull(message = "allergen은 필수입니다.")
        Boolean allergen,

        @Schema(
                description = "관리 화면과 공개 조회에서 사용할 정렬 순서입니다.",
                example = "10"
        )
        @NotNull(message = "sortOrder는 필수입니다.")
        Integer sortOrder
) {
}
