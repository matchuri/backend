package matchuri.backend.api.menu.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import matchuri.backend.api.menu.dto.response.AdminAttributeCategoryResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "관리자 attribute category 목록 조회 API의 공통 응답 envelope입니다.")
public record AdminAttributeCategoryListApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "성공 시 반환되는 전체 attribute category 목록입니다. 활성/비활성 데이터를 모두 포함합니다.")
        List<AdminAttributeCategoryResponse> data,

        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
