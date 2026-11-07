package matchuri.backend.domain.group.repository;

import matchuri.backend.domain.group.entity.GroupRecommendationReadinessStatus;

public interface GroupRecommendationReadinessRepositoryCustom {

    long countActiveMemberReadinessByRecommendationIdAndStatus(Long groupRecommendationId, Long roomId, GroupRecommendationReadinessStatus status);
}
