package matchuri.backend.domain.group.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRecommendationRepository extends JpaRepository<GroupRecommendation, Long> {

    boolean existsByRoomIdAndStatusInAndStartedAtAfter(
            Long roomId,
            Collection<GroupRecommendationStatus> statuses,
            LocalDateTime startedAt
    );

    Optional<GroupRecommendation> findByIdAndRoomId(Long id, Long roomId);

    Optional<GroupRecommendation> findFirstByRoomIdOrderByStartedAtDescIdDesc(Long roomId);

    Page<GroupRecommendation> findByRoomIdOrderByStartedAtDescIdDesc(Long roomId, Pageable pageable);

    List<GroupRecommendation> findByRoomIdInAndStatusInAndEndedAtIsNullAndStartedAtLessThanEqual(
            Collection<Long> roomIds,
            Collection<GroupRecommendationStatus> statuses,
            LocalDateTime startedAt
    );

}
