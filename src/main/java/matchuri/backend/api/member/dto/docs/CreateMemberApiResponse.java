package matchuri.backend.api.member.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.member.dto.response.CreateMemberResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "레거시 회원 가입 API의 공통 응답 envelope입니다.")
public record CreateMemberApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "성공 시 반환되는 회원 생성 payload입니다.")
        CreateMemberResponse data,

        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
