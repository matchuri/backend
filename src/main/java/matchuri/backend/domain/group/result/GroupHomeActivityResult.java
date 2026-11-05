package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import org.jspecify.annotations.Nullable;

public record GroupHomeActivityResult(
        Long groupId,
        String groupName,
        GroupRecommendationStatus type,
        Long recommendationId,
        LocalDateTime createdAt,
        @Nullable LocalDateTime startedAt,
        @Nullable LocalDateTime endedAt,
        @Nullable String selectedMenuName
) {
    public static GroupHomeActivityResult from(GroupRecommendation recommendation) {
        String selectedMenuName = recommendation.getStatus() == GroupRecommendationStatus.FINALIZED
                && recommendation.getSelectedCandidate() != null
                ? recommendation.getSelectedCandidate().getMenuItem().getName() : null;
        return new GroupHomeActivityResult(recommendation.getRoom().getId(), recommendation.getRoom().getName(),
                recommendation.getStatus(), recommendation.getId(), recommendation.getCreatedAt(),
                recommendation.getStartedAt(),
                recommendation.getEndedAt(), selectedMenuName);
    }

    public LocalDateTime activityAt() {
        if (endedAt != null) {
            return endedAt;
        }
        if (startedAt != null) {
            return startedAt;
        }
        return createdAt;
    }
}
