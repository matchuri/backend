package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record CancelGroupRecommendationResult(
        Long sessionId,
        GroupRecommendationStatus status,
        LocalDateTime canceledAt
) {
}
