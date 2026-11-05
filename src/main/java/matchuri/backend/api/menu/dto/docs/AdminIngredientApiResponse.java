package matchuri.backend.api.menu.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.menu.dto.response.AdminIngredientResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "관리자 ingredient 단건 응답 API의 공통 응답 envelope입니다.")
public record AdminIngredientApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "성공 시 반환되는 최신 ingredient 상태입니다.")
        AdminIngredientResponse data,

        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
