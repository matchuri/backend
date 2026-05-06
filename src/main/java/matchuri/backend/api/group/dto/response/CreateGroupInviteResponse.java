package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupInviteStatus;

public record CreateGroupInviteResponse(
        @Schema(description = "그룹 ID입니다.", example = "3001")
        Long groupId,

        @Schema(description = "생성된 초대 코드입니다.", example = "LUNCH42")
        String inviteCode,

        @Schema(description = "초대 코드 만료 시각입니다.", example = "2026-05-06T13:00:00")
        LocalDateTime expiresAt,

        @Schema(description = "초대 코드 상태입니다.", example = "ACTIVE")
        GroupInviteStatus status
) {
    public static CreateGroupInviteResponse mockActive() {
        return new CreateGroupInviteResponse(
                3001L,
                "LUNCH42",
                LocalDateTime.of(2026, 5, 6, 13, 0),
                GroupInviteStatus.ACTIVE
        );
    }
}
