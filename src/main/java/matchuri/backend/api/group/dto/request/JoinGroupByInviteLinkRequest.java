package matchuri.backend.api.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record JoinGroupByInviteLinkRequest(
        @Schema(
                description = "클라이언트 초대 URL에서 추출한 UUID 기반 토큰입니다.",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @NotBlank
        @Size(min = 36, max = 36)
        @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
        String token
) {
}
