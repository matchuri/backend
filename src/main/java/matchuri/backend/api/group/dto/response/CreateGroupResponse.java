package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.domain.group.entity.GroupRoomStatus;

public record CreateGroupResponse(
        @Schema(description = "생성된 그룹 ID입니다.", example = "3001")
        Long groupId,

        @Schema(description = "그룹 상태입니다.", example = "ACTIVE")
        GroupRoomStatus status
) {
    public static CreateGroupResponse mockActive() {
        return new CreateGroupResponse(3001L, GroupRoomStatus.ACTIVE);
    }
}
