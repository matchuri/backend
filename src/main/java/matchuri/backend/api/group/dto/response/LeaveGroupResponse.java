package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupMemberStatus;

public record LeaveGroupResponse(
        @Schema(description = "탈퇴한 그룹 ID입니다.", example = "3001")
        Long groupId,

        @Schema(description = "탈퇴 후 멤버 상태입니다.", example = "LEFT")
        GroupMemberStatus memberStatus,

        @Schema(description = "탈퇴 처리 시각입니다.", example = "2026-05-06T12:30:00")
        LocalDateTime leftAt
) {
    public static LeaveGroupResponse mockLeft() {
        return new LeaveGroupResponse(
                3001L,
                GroupMemberStatus.LEFT,
                LocalDateTime.of(2026, 5, 6, 12, 30)
        );
    }
}
