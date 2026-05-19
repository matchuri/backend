package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupInviteStatus;
import matchuri.backend.domain.group.entity.GroupMemberStatus;

public record RespondGroupInviteResponse(
        @Schema(description = "응답 처리한 초대 ID입니다.", example = "501")
        Long inviteId,

        @Schema(description = "초대 대상 그룹 ID입니다.", example = "3001")
        Long groupId,

        @Schema(description = "응답 후 초대 상태입니다.", example = "ACCEPTED")
        GroupInviteStatus inviteStatus,

        @Schema(description = "수락 후 그룹 멤버 상태입니다. 거절이면 null입니다.", example = "ACTIVE")
        GroupMemberStatus memberStatus,

        @Schema(description = "응답 처리 시각입니다.", example = "2026-05-19T12:10:00")
        LocalDateTime respondedAt
) {
}
