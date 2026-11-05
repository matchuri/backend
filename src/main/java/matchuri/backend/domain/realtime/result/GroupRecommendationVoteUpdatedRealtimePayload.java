package matchuri.backend.domain.realtime.result;

public record GroupRecommendationVoteUpdatedRealtimePayload(
        Long sessionId,
        RealtimeVoteProgressPayload voteProgress
) {
}
