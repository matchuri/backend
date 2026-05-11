package matchuri.backend.domain.recommendation.algorithm.output;

import java.util.Map;

public record MenuRecommendationCandidateResult(
        Long menuId,
        int rankNo,
        double score,
        Map<String, Object> scoreBreakdown,
        Map<String, Object> meta
) {
    public MenuRecommendationCandidateResult {
        scoreBreakdown = scoreBreakdown == null ? Map.of() : Map.copyOf(scoreBreakdown);
        meta = meta == null ? Map.of() : Map.copyOf(meta);
    }
}
