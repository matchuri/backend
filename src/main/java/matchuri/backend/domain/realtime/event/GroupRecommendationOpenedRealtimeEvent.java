package matchuri.backend.domain.realtime.event;

import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import matchuri.backend.domain.group.result.GroupRecommendationCandidateResult;
import matchuri.backend.domain.group.result.GroupVoteProgressResult;

public record GroupRecommendationOpenedRealtimeEvent(
        Long groupId,
        Long sessionId,
        GroupRecommendationStatus status,
        List<GroupRecommendationCandidateResult> candidates,
        GroupVoteProgressResult voteProgress
) {
}
