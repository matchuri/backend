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

        @Schema(description = "현재 로그인한 회원 본인 여부입니다.", example = "true")
        boolean isMe,

        @Schema(description = "해당 회원의 투표 여부입니다.", example = "true")
        boolean voted,

        @Schema(description = "해당 회원이 투표한 후보 ID입니다. 투표하지 않았으면 null입니다.", example = "8001")
        Long candidateId
) {
}
