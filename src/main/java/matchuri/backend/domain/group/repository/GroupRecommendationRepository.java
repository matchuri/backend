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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRecommendationRepository extends JpaRepository<GroupRecommendation, Long>,
        GroupRecommendationRepositoryCustom {

    @Query("""
            select recommendation from GroupRecommendation recommendation
            join fetch recommendation.room room
            left join fetch recommendation.selectedCandidate candidate
            left join fetch candidate.menuItem
            where room.status <> matchuri.backend.domain.group.entity.GroupRoomStatus.DELETED
              and exists (
                  select membership.id from GroupRoomMember membership
                  where membership.room = room and membership.member.id = :memberId
                    and membership.status = matchuri.backend.domain.group.entity.GroupMemberStatus.ACTIVE
              )
            order by recommendation.createdAt desc, recommendation.id desc
            """)
    List<GroupRecommendation> findHistoryForActiveMember(@Param("memberId") Long memberId);

    boolean existsByRoomIdAndStatusInAndCreatedAtAfter(
            Long roomId,
            Collection<GroupRecommendationStatus> statuses,
            LocalDateTime createdAt
    );

    Optional<GroupRecommendation> findByIdAndRoomId(Long id, Long roomId);

    Optional<GroupRecommendation> findFirstByRoomIdOrderByCreatedAtDescIdDesc(Long roomId);

    Page<GroupRecommendation> findByRoomIdOrderByCreatedAtDescIdDesc(Long roomId, Pageable pageable);

    List<GroupRecommendation> findByRoomIdInAndStatusInAndEndedAtIsNullAndCreatedAtLessThanEqual(
            Collection<Long> roomIds,
            Collection<GroupRecommendationStatus> statuses,
            LocalDateTime createdAt
    );

}
