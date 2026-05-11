package matchuri.backend.domain.recommendation.algorithm.output;

import java.util.List;
import matchuri.backend.domain.recommendation.algorithm.RecommendationAlgorithmType;

public record MenuRecommendationResult(
        RecommendationAlgorithmType algorithmType,
        String algorithmVersion,
        List<MenuRecommendationCandidateResult> candidates
) {
    public MenuRecommendationResult {
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
    }
}
