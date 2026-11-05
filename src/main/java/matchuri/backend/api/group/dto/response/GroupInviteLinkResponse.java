package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record GroupInviteLinkResponse(
        @Schema(description = "초대 링크가 가리키는 그룹 ID입니다.", example = "3001")
        Long groupId,

        @Schema(description = "클라이언트 초대 URL 끝에 붙이는 UUID 기반 토큰입니다.", example = "550e8400-e29b-41d4-a716-446655440000")
        String token,

        @Schema(description = "초대 링크 만료 시각입니다.", example = "2026-08-15T12:00:00")
        LocalDateTime expiresAt
) {
}
