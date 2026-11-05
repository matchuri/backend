package matchuri.backend.domain.group.repository.impl;

import static matchuri.backend.domain.group.entity.QGroupRecommendationReadiness.groupRecommendationReadiness;
import static matchuri.backend.domain.group.entity.QGroupRoomMember.groupRoomMember;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.entity.GroupMemberStatus;
import matchuri.backend.domain.group.entity.GroupRecommendationReadinessStatus;
import matchuri.backend.domain.group.repository.GroupRecommendationReadinessRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupRecommendationReadinessRepositoryImpl
        implements GroupRecommendationReadinessRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public long countActiveMemberReadinessByRecommendationIdAndStatus(Long groupRecommendationId, Long roomId, GroupRecommendationReadinessStatus status) {
        Long count = jpaQueryFactory
                .select(groupRecommendationReadiness.count())
                .from(groupRecommendationReadiness)
                .join(groupRoomMember).on(
                        groupRoomMember.member.id.eq(groupRecommendationReadiness.member.id),
                        groupRoomMember.room.id.eq(roomId),
                        groupRoomMember.status.eq(GroupMemberStatus.ACTIVE)
                )
                .where(
                        groupRecommendationReadiness.groupRecommendation.id.eq(groupRecommendationId),
                        groupRecommendationReadiness.status.eq(status)
                )
                .fetchOne();
        return count == null ? 0L : count;
    }
}
