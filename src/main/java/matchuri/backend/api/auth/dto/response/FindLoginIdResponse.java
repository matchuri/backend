package matchuri.backend.api.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 ID 찾기 응답입니다.")
public record FindLoginIdResponse(
        @Schema(description = "인증된 이메일에 연결된 자체 로그인 ID입니다.", example = "tester01")
        String loginId
) {
}
