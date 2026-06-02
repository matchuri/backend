package matchuri.backend.domain.realtime.result;

import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record GroupRecommendationReadinessUpdatedRealtimePayload(
        Long sessionId,
        GroupRecommendationStatus status,
        Long readyMemberId,
        String readyMemberNickname,
        RealtimeReadinessProgressPayload readinessProgress
) {
}
