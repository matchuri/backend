package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import org.jspecify.annotations.Nullable;

public record GroupRecommendationV2SummaryResult(
        Long sessionId,
        GroupRecommendationStatus status,
        @Nullable String selectedMenuName,
        LocalDateTime createdAt,
        @Nullable LocalDateTime startedAt,
        @Nullable LocalDateTime endedAt
) {
    public static GroupRecommendationV2SummaryResult from(GroupRecommendation recommendation) {
        String selectedMenuName = recommendation.getStatus() == GroupRecommendationStatus.FINALIZED
                && recommendation.getSelectedCandidate() != null
                ? recommendation.getSelectedCandidate().getMenuItem().getName()
                : null;

        return new GroupRecommendationV2SummaryResult(
                recommendation.getId(),
                recommendation.getStatus(),
                selectedMenuName,
                recommendation.getCreatedAt(),
                recommendation.getStartedAt(),
                recommendation.getEndedAt()
        );
    }
}
