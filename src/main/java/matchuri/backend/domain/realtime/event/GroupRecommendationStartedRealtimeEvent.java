package matchuri.backend.domain.realtime.event;

import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import matchuri.backend.domain.group.result.GroupRecommendationReadinessProgressResult;

public record GroupRecommendationStartedRealtimeEvent(
        Long groupId,
        Long sessionId,
        Long actorMemberId,
        GroupRecommendationStatus status,
        GroupRecommendationReadinessProgressResult readinessProgress
) {
}
