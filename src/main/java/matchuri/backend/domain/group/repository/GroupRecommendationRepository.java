package matchuri.backend.domain.group.repository;

import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRecommendationRepository extends JpaRepository<GroupRecommendation, Long> {

    boolean existsByRoomIdAndStatus(Long roomId, GroupRecommendationStatus status);
}
