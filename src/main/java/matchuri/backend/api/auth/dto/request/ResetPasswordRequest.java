package matchuri.backend.api.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import matchuri.backend.domain.member.entity.Member;

@Schema(description = "비밀번호 재설정 요청입니다.")
public record ResetPasswordRequest(
        @Schema(
                description = "비밀번호를 재설정할 자체 로그인 ID입니다.",
                example = "tester01",
                maxLength = Member.LOGIN_ID_MAX_SIZE
        )
        @NotBlank(message = "loginId는 비어 있을 수 없습니다.")
        @Size(max = Member.LOGIN_ID_MAX_SIZE, message = "loginId는 " + Member.LOGIN_ID_MAX_SIZE + "자를 초과할 수 없습니다.")
        @Pattern(regexp = Member.LOGIN_ID_PATTERN, message = "loginId는 영문, 숫자, 점(.), 밑줄(_), 하이픈(-)만 사용할 수 있습니다.")
        String loginId,

        @Schema(
                description = "RESET_PASSWORD 목적 이메일 인증 확인 API에서 발급받은 token입니다.",
                example = "ev_q3JxFrSxYk4zJw2zq3ZpQh0a3z9q0x1y2z3A4b5C6dE"
        )
        @NotBlank(message = "emailVerificationToken은 비어 있을 수 없습니다.")
        String emailVerificationToken,

        @Schema(
                description = "새 비밀번호입니다. 로그인 비밀번호 정책과 같은 길이 제약을 사용합니다.",
                example = "N3wP@ssw0rd!",
                minLength = Member.PASSWORD_MIN_SIZE,
                maxLength = Member.PASSWORD_MAX_SIZE
        )
        @NotBlank(message = "newPassword는 비어 있을 수 없습니다.")
        @Size(
                min = Member.PASSWORD_MIN_SIZE,
                max = Member.PASSWORD_MAX_SIZE,
                message = "newPassword는 " + Member.PASSWORD_MIN_SIZE + "자 이상 " + Member.PASSWORD_MAX_SIZE + "자 이하여야 합니다."
        )
        String newPassword
) {
}
