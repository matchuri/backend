package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import matchuri.backend.domain.group.entity.GroupRoomStatus;

public record GroupSummaryResult(
        Long id,
        String name,
        GroupRoomStatus status,
        int memberCount,
        GroupRecommendationStatus latestRecommendationStatus,
        LocalDateTime createdAt
) {
}
