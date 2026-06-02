package matchuri.backend.domain.realtime.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record GroupRecommendationFinalizedRealtimePayload(
        Long sessionId,
        GroupRecommendationStatus status,
        LocalDateTime finalizedAt,
        RealtimeCandidatePayload finalCandidate
) {
}
