package matchuri.backend.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "loginId는 비어 있을 수 없습니다.")
        @Size(max = 50, message = "loginId는 50자를 초과할 수 없습니다.")
        String loginId,

        @NotBlank(message = "password는 비어 있을 수 없습니다.")
        @Size(min = 8, max = 100, message = "password는 8자 이상 100자 이하여야 합니다.")
        String password
) {
}
