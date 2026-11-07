package matchuri.backend.domain.group.result;

import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record GroupRecommendationReadinessResult(
        Long sessionId,
        GroupRecommendationStatus status,
        GroupRecommendationReadinessProgressResult progress,
        List<GroupRecommendationReadinessMemberResult> members
) {
}
