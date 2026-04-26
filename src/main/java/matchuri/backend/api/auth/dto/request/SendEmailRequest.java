package matchuri.backend.api.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import matchuri.backend.domain.auth.entity.EmailVerificationPurpose;

@Schema(description = "이메일 인증 코드 발송 요청입니다.")
public record SendEmailRequest(
        @Schema(description = "인증 코드를 받을 이메일입니다.", example = "tester@example.com", maxLength = 150)
        @NotBlank(message = "email은 비어 있을 수 없습니다.")
        @Email(message = "email은 올바른 이메일 형식이어야 합니다.")
        @Size(max = 150, message = "email은 150자 이하여야 합니다.")
        String email,

        @Schema(description = "이메일 인증 목적입니다.", example = "RESET_PASSWORD")
        @NotNull(message = "purpose는 필수입니다.")
        EmailVerificationPurpose purpose,

        @Schema(description = "비밀번호 재설정 목적에서 확인할 로그인 ID입니다.", example = "tester01", maxLength = 50)
        @Size(max = 50, message = "loginId는 50자 이하여야 합니다.")
        String loginId
) {
}
