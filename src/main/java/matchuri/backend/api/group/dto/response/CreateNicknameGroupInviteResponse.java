package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupInviteStatus;

public record CreateNicknameGroupInviteResponse(
        @Schema(description = "생성된 초대 ID입니다.", example = "501")
        Long inviteId,

        @Schema(description = "초대 대상 그룹 ID입니다.", example = "3001")
        Long groupId,

        @Schema(description = "초대 대상 그룹 이름입니다.", example = "오늘 점심 메뉴 회의")
        String groupName,

        @Schema(description = "초대 대상 회원 ID입니다.", example = "42")
        Long targetMemberId,

        @Schema(description = "초대 대상 회원 닉네임입니다.", example = "점심탐험가")
        String targetNickname,

        @Schema(description = "초대 만료 시각입니다.", example = "2026-05-20T12:00:00")
        LocalDateTime expiresAt,

        @Schema(description = "초대 상태입니다.", example = "PENDING")
        GroupInviteStatus status
) {
}
