package matchuri.backend.api.auth.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.auth.dto.response.ConfirmEmailResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "이메일 인증 코드 확인 API의 공통 응답 envelope입니다.")
public record ConfirmEmailVerificationApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "성공 시 반환되는 이메일 인증 확인 payload입니다.")
        ConfirmEmailResponse data,

        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
