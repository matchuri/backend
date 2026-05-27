package matchuri.backend.domain.group.repository;

import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupRecommendationReadiness;
import matchuri.backend.domain.group.entity.GroupRecommendationReadinessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRecommendationReadinessRepository
        extends JpaRepository<GroupRecommendationReadiness, Long> {

    Optional<GroupRecommendationReadiness> findByGroupRecommendationIdAndMemberId(
            Long groupRecommendationId,
            Long memberId
    );

    @Query("""
            select count(readiness)
            from GroupRecommendationReadiness readiness
            join GroupRoomMember groupMember
              on groupMember.member.id = readiness.member.id
             and groupMember.room.id = :roomId
             and groupMember.status = matchuri.backend.domain.group.entity.GroupMemberStatus.ACTIVE
            where readiness.groupRecommendation.id = :groupRecommendationId
              and readiness.status = :status
            """)
    long countActiveMemberReadinessByRecommendationIdAndStatus(
            @Param("groupRecommendationId") Long groupRecommendationId,
            @Param("roomId") Long roomId,
            @Param("status") GroupRecommendationReadinessStatus status
    );
}
