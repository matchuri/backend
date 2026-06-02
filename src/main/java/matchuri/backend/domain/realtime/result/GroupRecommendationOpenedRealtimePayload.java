package matchuri.backend.domain.realtime.result;

import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record GroupRecommendationOpenedRealtimePayload(
        Long sessionId,
        GroupRecommendationStatus status,
        List<RealtimeCandidatePayload> candidates,
        RealtimeVoteProgressPayload voteProgress
) {
}
