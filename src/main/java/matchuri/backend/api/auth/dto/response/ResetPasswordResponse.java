package matchuri.backend.api.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 재설정 응답입니다.")
public record ResetPasswordResponse(
        @Schema(description = "비밀번호 재설정 성공 여부입니다.", example = "true")
        boolean reset
) {
}
