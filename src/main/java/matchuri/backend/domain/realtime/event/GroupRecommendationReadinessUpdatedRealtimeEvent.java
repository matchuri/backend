package matchuri.backend.domain.realtime.event;

import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import matchuri.backend.domain.group.result.GroupRecommendationReadinessProgressResult;

public record GroupRecommendationReadinessUpdatedRealtimeEvent(
        Long groupId,
        Long sessionId,
        Long readyMemberId,
        String readyMemberNickname,
        GroupRecommendationStatus status,
        GroupRecommendationReadinessProgressResult readinessProgress
) {
}
