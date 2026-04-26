package matchuri.backend.api.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 ID 찾기 요청입니다.")
public record FindLoginIdRequest(
        @Schema(
                description = "FIND_LOGIN_ID 목적 이메일 인증 확인 API에서 발급받은 token입니다.",
                example = "ev_q3JxFrSxYk4zJw2zq3ZpQh0a3z9q0x1y2z3A4b5C6dE"
        )
        @NotBlank(message = "emailVerificationToken은 비어 있을 수 없습니다.")
        String emailVerificationToken
) {
}
