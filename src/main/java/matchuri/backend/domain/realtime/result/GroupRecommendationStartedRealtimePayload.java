package matchuri.backend.domain.realtime.result;

import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record GroupRecommendationStartedRealtimePayload(
        Long sessionId,
        GroupRecommendationStatus status,
        RealtimeReadinessProgressPayload readinessProgress
) {
}
