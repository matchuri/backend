package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record GroupRecommendationSummaryResult(
        Long sessionId,
        GroupRecommendationStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
    public static GroupRecommendationSummaryResult from(GroupRecommendation recommendation) {
        return new GroupRecommendationSummaryResult(
                recommendation.getId(),
                recommendation.getStatus(),
                recommendation.getStartedAt(),
                recommendation.getEndedAt()
        );
    }
}
