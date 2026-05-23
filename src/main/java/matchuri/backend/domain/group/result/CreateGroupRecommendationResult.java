package matchuri.backend.domain.group.result;

import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record CreateGroupRecommendationResult(
        Long sessionId,
        GroupRecommendationStatus status,
        List<GroupRecommendationCandidateResult> candidates
) {
}
