package matchuri.backend.domain.group.result;

public record GroupRecommendationReadinessProgressResult(
        int totalMemberCount,
        int readyMemberCount,
        boolean allReady
) {
    public static GroupRecommendationReadinessProgressResult of(int totalMemberCount, int readyMemberCount) {
        return new GroupRecommendationReadinessProgressResult(
                totalMemberCount,
                readyMemberCount,
                totalMemberCount > 0 && totalMemberCount == readyMemberCount
        );
    }
}
