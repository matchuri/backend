package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.domain.group.entity.GroupMemberStatus;

public record JoinGroupResponse(
        @Schema(description = "참여한 그룹 ID입니다.", example = "3001")
        Long groupId,

        @Schema(description = "그룹 멤버 상태입니다.", example = "ACTIVE")
        GroupMemberStatus memberStatus
) {
    public static JoinGroupResponse mockJoined() {
        return new JoinGroupResponse(3001L, GroupMemberStatus.ACTIVE);
    }
}
