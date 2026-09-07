package matchuri.backend.domain.group.repository;

import matchuri.backend.domain.group.entity.GroupRecommendationStatus;

public record GroupRecommendationStatusQueryRow(
        Long roomId,
        GroupRecommendationStatus status
) {
}
