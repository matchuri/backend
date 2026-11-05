package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import org.jspecify.annotations.Nullable;

public record GroupRecommendationSummaryResult(
        Long sessionId,
        GroupRecommendationStatus status,
        LocalDateTime createdAt,
        @Nullable LocalDateTime startedAt,
        @Nullable LocalDateTime endedAt
) {
    public static GroupRecommendationSummaryResult from(GroupRecommendation recommendation) {
        return new GroupRecommendationSummaryResult(
                recommendation.getId(),
                recommendation.getStatus(),
                recommendation.getCreatedAt(),
                recommendation.getStartedAt(),
                recommendation.getEndedAt()
        );
    }
}
