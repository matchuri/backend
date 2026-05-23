package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record GroupRecommendationResult(
        Long sessionId,
        GroupRecommendationStatus status,
        List<GroupRecommendationCandidateResult> candidates,
        GroupVoteProgressResult voteProgress,
        GroupRecommendationCandidateResult finalCandidate,
        LocalDateTime createdAt
) {
}
