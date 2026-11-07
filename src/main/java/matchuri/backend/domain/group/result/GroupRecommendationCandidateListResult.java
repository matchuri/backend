package matchuri.backend.domain.group.result;

import java.util.List;

public record GroupRecommendationCandidateListResult(
        Long sessionId,
        List<GroupRecommendationCandidateResult> candidates
) {
}
