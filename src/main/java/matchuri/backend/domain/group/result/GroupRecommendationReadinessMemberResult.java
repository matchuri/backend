package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupRecommendationReadinessStatus;

public record GroupRecommendationReadinessMemberResult(
        Long memberId,
        String nickname,
        GroupMemberRole role,
        boolean ready,
        GroupRecommendationReadinessStatus readinessStatus,
        LocalDateTime readinessUpdatedAt
) {
}
