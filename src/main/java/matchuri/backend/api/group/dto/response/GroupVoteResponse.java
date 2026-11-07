package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record GroupVoteResponse(
        @Schema(description = "투표 ID입니다.", example = "91001")
        Long voteId,

        @Schema(description = "투표한 후보 ID입니다.", example = "8001")
        Long candidateId,

        @Schema(description = "투표 시각입니다.", example = "2026-05-06T12:20:00")
        LocalDateTime votedAt
) {
}
