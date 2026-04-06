package matchuri.backend.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Schema(
                description = "일반 로그인에 사용하는 loginId입니다.",
                example = "tester01",
                maxLength = 50
        )
        @NotBlank(message = "loginId는 비어 있을 수 없습니다.")
        @Size(max = 50, message = "loginId는 50자를 초과할 수 없습니다.")
        String loginId,

        @Schema(
                description = "일반 로그인 비밀번호입니다. 평문은 요청 시에만 사용되며 서버에는 해시로 저장됩니다.",
                example = "P@ssw0rd!",
                minLength = 8,
                maxLength = 100
        )
        @NotBlank(message = "password는 비어 있을 수 없습니다.")
        @Size(min = 8, max = 100, message = "password는 8자 이상 100자 이하여야 합니다.")
        String password
) {
}
