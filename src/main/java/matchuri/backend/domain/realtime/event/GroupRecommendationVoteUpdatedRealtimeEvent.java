package matchuri.backend.domain.realtime.event;

import matchuri.backend.domain.group.result.GroupVoteProgressResult;

public record GroupRecommendationVoteUpdatedRealtimeEvent(
        Long groupId,
        Long sessionId,
        GroupVoteProgressResult voteProgress
) {
}
