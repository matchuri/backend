package matchuri.backend.api.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginIdExistsResponse(
        @Schema(description = "중복 확인한 로그인 ID", example = "tester01")
        String loginId,
        @Schema(description = "이미 존재하는 로그인 ID인지 여부. true면 이미 사용 중, false면 회원 가입 가능", example = "true")
        boolean exists
) {
}
