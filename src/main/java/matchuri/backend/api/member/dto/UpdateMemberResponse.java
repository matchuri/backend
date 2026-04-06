package matchuri.backend.api.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record UpdateMemberResponse(
        @Schema(description = "수정 대상 회원 ID입니다.", example = "1")
        Long id,

        @Schema(description = "서버가 기록한 마지막 수정 시각입니다.", example = "2026-04-07T10:20:45")
        LocalDateTime updatedAt
) {
}
