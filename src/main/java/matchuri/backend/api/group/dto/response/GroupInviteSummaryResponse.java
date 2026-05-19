package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupInviteStatus;

public record GroupInviteSummaryResponse(
        @Schema(description = "초대 ID입니다.", example = "501")
        Long inviteId,

        @Schema(description = "초대 대상 그룹 ID입니다.", example = "3001")
        Long groupId,

        @Schema(description = "초대 대상 그룹 이름입니다.", example = "맛집 탐방 모임")
        String groupName,

        @Schema(description = "초대한 회원 ID입니다.", example = "11")
        Long requestMemberId,

        @Schema(description = "초대한 회원 닉네임입니다.", example = "나는야 임영웅")
        String requestMemberNickname,

        @Schema(description = "초대 상태입니다.", example = "PENDING")
        GroupInviteStatus status,

        @Schema(description = "초대 만료 시각입니다.", example = "2026-05-20T12:00:00")
        LocalDateTime expiresAt,

        @Schema(description = "초대 생성 시각입니다.", example = "2026-05-19T12:00:00")
        LocalDateTime createdAt
) {
}
