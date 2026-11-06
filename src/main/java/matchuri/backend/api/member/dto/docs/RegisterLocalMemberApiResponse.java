package matchuri.backend.api.member.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.member.dto.response.RegisterLocalMemberResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "자체 회원가입 통합 API의 공통 응답 envelope입니다.")
public record RegisterLocalMemberApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "성공 시 반환되는 회원가입 완료 payload입니다.")
        RegisterLocalMemberResponse data,

        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
