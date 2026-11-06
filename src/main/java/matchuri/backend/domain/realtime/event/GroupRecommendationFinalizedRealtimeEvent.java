package matchuri.backend.domain.realtime.event;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import matchuri.backend.domain.group.result.GroupRecommendationCandidateResult;

public record GroupRecommendationFinalizedRealtimeEvent(
        Long groupId,
        Long sessionId,
        Long actorMemberId,
        GroupRecommendationStatus status,
        GroupRecommendationCandidateResult finalCandidate,
        LocalDateTime finalizedAt
) {
}
