package matchuri.backend.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import matchuri.backend.domain.member.entity.Member;

public record LoginRequest(
        @Schema(
                description = "일반 로그인에 사용하는 loginId입니다.",
                example = "tester01",
                maxLength = Member.LOGIN_ID_MAX_SIZE
        )
        @NotBlank(message = "loginId는 비어 있을 수 없습니다.")
        @Size(max = Member.LOGIN_ID_MAX_SIZE, message = "loginId는 " + Member.LOGIN_ID_MAX_SIZE + "자를 초과할 수 없습니다.")
        String loginId,

        @Schema(
                description = "일반 로그인 비밀번호입니다. 평문은 요청 시에만 사용되며 서버에는 해시로 저장됩니다.",
                example = "P@ssw0rd!",
                minLength = Member.PASSWORD_MIN_SIZE,
                maxLength = Member.PASSWORD_MAX_SIZE
        )
        @NotBlank(message = "password는 비어 있을 수 없습니다.")
        @Size(
                min = Member.PASSWORD_MIN_SIZE,
                max = Member.PASSWORD_MAX_SIZE,
                message = "password는 " + Member.PASSWORD_MIN_SIZE + "자 이상 " + Member.PASSWORD_MAX_SIZE + "자 이하여야 합니다."
        )
        String password
) {
}
