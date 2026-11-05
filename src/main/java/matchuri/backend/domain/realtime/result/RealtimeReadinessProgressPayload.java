package matchuri.backend.domain.realtime.result;

import matchuri.backend.domain.group.result.GroupRecommendationReadinessProgressResult;

public record RealtimeReadinessProgressPayload(
        int totalMemberCount,
        int readyMemberCount,
        boolean allReady
) {
    public static RealtimeReadinessProgressPayload from(GroupRecommendationReadinessProgressResult progress) {
        return new RealtimeReadinessProgressPayload(
                progress.totalMemberCount(),
                progress.readyMemberCount(),
                progress.allReady()
        );
    }
}
