package matchuri.backend.api.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 인증 코드 확인 응답입니다.")
public record ConfirmEmailResponse(
        @Schema(description = "인증 성공 여부입니다.", example = "true")
        boolean verified,

        @Schema(description = "후속 회원가입/계정 복구 API에 전달할 이메일 인증 token입니다.")
        String emailVerificationToken,

        @Schema(description = "이메일 인증 token 유효 시간(초)입니다.", example = "600")
        long expiresIn
) {
}
