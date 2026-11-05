package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record FinalizeGroupRecommendationResult(
        Long sessionId,
        GroupRecommendationStatus status,
        GroupRecommendationCandidateResult finalCandidate,
        LocalDateTime finalizedAt
) {
}
