package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GroupInviteLinkPreviewResponse(
        @Schema(description = "초대 링크가 가리키는 그룹명입니다.", example = "오늘 점심 메뉴 회의")
        String groupName,
        @Schema(description = "그룹 방장의 현재 닉네임입니다.", example = "점심탐험가")
        String ownerNickname,
        @Schema(description = "활성 그룹원 수입니다. 방장을 포함합니다.", example = "3")
        int memberCount
) {
}
