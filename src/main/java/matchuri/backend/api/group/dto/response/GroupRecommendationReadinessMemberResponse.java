package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.domain.group.entity.GroupMemberRole;

public record GroupRecommendationReadinessMemberResponse(
        @Schema(description = "회원 ID입니다.", example = "1")
        Long memberId,

        @Schema(description = "회원 닉네임입니다.", example = "점심탐험가")
        String nickname,

        @Schema(description = "그룹 내 역할입니다.", example = "OWNER")
        GroupMemberRole role,

        @Schema(description = "현재 준비 완료 여부입니다.", example = "true")
        boolean ready
) {
}
