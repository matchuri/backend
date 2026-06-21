package matchuri.backend.domain.group.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRecommendationRepository extends JpaRepository<GroupRecommendation, Long> {

    boolean existsByRoomIdAndStatusInAndStartedAtAfter(
            Long roomId,
            Collection<GroupRecommendationStatus> statuses,
            LocalDateTime startedAt
    );

    Optional<GroupRecommendation> findByIdAndRoomId(Long id, Long roomId);

    Optional<GroupRecommendation> findFirstByRoomIdAndStatusInAndStartedAtAfterOrderByStartedAtDescIdDesc(
            Long roomId,
            Collection<GroupRecommendationStatus> statuses,
            LocalDateTime startedAt
    );

    @Query("""
            select recommendation
            from GroupRecommendation recommendation
            where recommendation.room.id = :roomId
              and (
                    recommendation.status not in :activeStatuses
                    or recommendation.startedAt > :activeThreshold
              )
            order by recommendation.startedAt desc, recommendation.id desc
            """)
    Page<GroupRecommendation> findVisibleByRoomIdOrderByStartedAtDescIdDesc(
            @Param("roomId") Long roomId,
            @Param("activeStatuses") Collection<GroupRecommendationStatus> activeStatuses,
            @Param("activeThreshold") LocalDateTime activeThreshold,
            Pageable pageable
    );
}
