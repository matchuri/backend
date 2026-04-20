package matchuri.backend.api.menu.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import matchuri.backend.api.menu.dto.response.RestrictionIngredientResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "restriction ingredient 목록 조회 API의 공통 응답 envelope입니다.")
public record RestrictionIngredientListApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "성공 시 반환되는 활성 restriction ingredient 목록입니다.")
        List<RestrictionIngredientResponse> data,

        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
