package matchuri.backend.api.auth.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.auth.dto.response.LoginResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "로그인/토큰 재발급 계열 API의 공통 응답 envelope입니다.")
public record LoginApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "성공 시 반환되는 로그인 응답 payload입니다.")
        LoginResponse data,

        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
