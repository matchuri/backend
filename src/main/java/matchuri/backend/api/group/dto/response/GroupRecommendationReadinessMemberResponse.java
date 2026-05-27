package matchuri.backend.api.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupRecommendationReadinessStatus;

public record GroupRecommendationReadinessMemberResponse(
        @Schema(description = "회원 ID입니다.", example = "1")
        Long memberId,

        @Schema(description = "회원 닉네임입니다.", example = "점심탐험가")
        String nickname,

        @Schema(description = "그룹 내 역할입니다.", example = "OWNER")
        GroupMemberRole role,

        @Schema(description = "현재 준비 완료 여부입니다.", example = "true")
        boolean ready,

        @Schema(description = "준비 상태입니다. 아직 액션이 없으면 null입니다.", example = "READY", nullable = true)
        GroupRecommendationReadinessStatus readinessStatus,

        @Schema(description = "준비 상태가 마지막으로 변경된 시각입니다. 아직 액션이 없으면 null입니다.", example = "2026-05-26T12:05:00", nullable = true)
        LocalDateTime readinessUpdatedAt
) {
}
