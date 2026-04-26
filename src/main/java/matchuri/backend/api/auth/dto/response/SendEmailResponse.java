package matchuri.backend.api.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 인증 코드 발송 접수 응답입니다.")
public record SendEmailResponse(
        @Schema(description = "발송 요청이 접수되었는지 여부입니다. 계정 존재 여부와 무관하게 true를 반환합니다.", example = "true")
        boolean accepted,

        @Schema(description = "다음 재발송 요청을 권장하는 대기 시간(초)입니다.", example = "60")
        long resendAvailableAfterSeconds
) {
}
