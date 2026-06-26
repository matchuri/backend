package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.domain.group.entity.GroupMemberRole;

public record GroupMemberVoteResponse(
        @Schema(description = "회원 ID입니다.", example = "1")
        Long memberId,

        @Schema(description = "회원 닉네임입니다.", example = "점심탐험가")
        String nickname,

        @Schema(description = "그룹 내 역할입니다.", example = "OWNER")
        GroupMemberRole role,

        @Schema(description = "해당 회원의 투표 여부입니다.", example = "true")
        boolean voted
) {
}
