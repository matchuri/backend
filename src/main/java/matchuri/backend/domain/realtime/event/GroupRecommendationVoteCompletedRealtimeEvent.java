package matchuri.backend.domain.realtime.event;

import matchuri.backend.domain.group.result.GroupVoteProgressResult;

public record GroupRecommendationVoteCompletedRealtimeEvent(
        Long groupId,
        Long sessionId,
        Long ownerMemberId,
        GroupVoteProgressResult voteProgress
) {
}
