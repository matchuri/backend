package matchuri.backend.api.member.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.member.dto.response.LoginIdExistsResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "로그인 ID 중복 확인 API의 공통 응답 envelope입니다.")
public record LoginIdExistsApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "성공 시 반환되는 로그인 ID 중복 확인 payload입니다.")
        LoginIdExistsResponse data,

        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
