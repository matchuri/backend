package matchuri.backend.domain.realtime.result;

public record GroupRecommendationVoteCompletedRealtimePayload(
        Long sessionId,
        RealtimeVoteProgressPayload voteProgress,
        boolean finalizeRequired
) {
}
