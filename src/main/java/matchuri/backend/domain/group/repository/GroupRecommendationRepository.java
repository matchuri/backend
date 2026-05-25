package matchuri.backend.domain.group.repository;

import java.util.Collection;
import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRecommendationRepository extends JpaRepository<GroupRecommendation, Long> {

    boolean existsByRoomIdAndStatus(Long roomId, GroupRecommendationStatus status);

    boolean existsByRoomIdAndStatusIn(Long roomId, Collection<GroupRecommendationStatus> statuses);

    Optional<GroupRecommendation> findByIdAndRoomId(Long id, Long roomId);

    Optional<GroupRecommendation> findFirstByRoomIdAndStatusOrderByStartedAtDesc(
            Long roomId,
            GroupRecommendationStatus status
    );

    Page<GroupRecommendation> findByRoomIdOrderByStartedAtDescIdDesc(Long roomId, Pageable pageable);
}
