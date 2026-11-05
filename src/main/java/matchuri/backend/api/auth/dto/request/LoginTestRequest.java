package matchuri.backend.api.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LoginTestRequest(
        @Schema(description = "JWT를 발급할 로컬 테스트 회원 ID입니다.", example = "1")
        @NotNull(message = "memberId는 null일 수 없습니다.")
        @Positive(message = "memberId는 양수여야 합니다.")
        Long memberId
) {
}
