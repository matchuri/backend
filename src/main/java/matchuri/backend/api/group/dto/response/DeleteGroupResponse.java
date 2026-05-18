package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRoomStatus;

public record DeleteGroupResponse(
        @Schema(description = "삭제된 그룹 ID입니다.", example = "3001")
        Long groupId,

        @Schema(description = "삭제 후 그룹 상태입니다.", example = "DELETED")
        GroupRoomStatus status,

        @Schema(description = "삭제 처리 시각입니다.", example = "2026-05-18T12:30:00")
        LocalDateTime deletedAt
) {
}
