package matchuri.backend.domain.group.result;

import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record ReadyGroupRecommendationResult(
        Long sessionId,
        GroupRecommendationStatus status,
        GroupRecommendationReadinessProgressResult readiness,
        List<GroupRecommendationCandidateResult> candidates
) {
}
