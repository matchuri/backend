package matchuri.backend.api.auth.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.auth.dto.response.FindLoginIdResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "로그인 ID 찾기 API의 공통 응답 envelope입니다.")
public record FindLoginIdApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "성공 시 반환되는 로그인 ID 찾기 payload입니다.")
        FindLoginIdResponse data,

        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
