package matchuri.backend.domain.group.repository;

import matchuri.backend.domain.group.entity.GroupRecommendationReadiness;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRecommendationReadinessRepository
        extends JpaRepository<GroupRecommendationReadiness, Long> {
}
