package matchuri.backend.domain.group.repository.impl;

import static matchuri.backend.domain.group.entity.QGroupRoomMember.groupRoomMember;
import static matchuri.backend.domain.group.entity.QGroupRoom.groupRoom;
import static matchuri.backend.domain.member.entity.QMember.member;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupMemberStatus;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.repository.GroupRoomMemberCountRow;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepositoryCustom;
import matchuri.backend.domain.member.entity.MemberStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupRoomMemberRepositoryImpl implements GroupRoomMemberRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<GroupRoomMemberCountRow> countMembersByRoomIdsAndStatus(List<Long> roomIds, GroupMemberStatus memberStatus) {
        if (roomIds.isEmpty()) {
            return List.of();
        }

        return jpaQueryFactory
                .select(Projections.constructor(
                        GroupRoomMemberCountRow.class,
                        groupRoomMember.room.id,
                        groupRoomMember.count()
                ))
                .from(groupRoomMember)
                .join(groupRoomMember.member, member)
                .where(
                        groupRoomMember.room.id.in(roomIds),
                        groupRoomMember.status.eq(memberStatus),
                        member.status.eq(MemberStatus.ACTIVE)
                )
                .groupBy(groupRoomMember.room.id)
                .fetch();
    }

    @Override
    public Page<GroupRoomMember> findMyActiveMemberships(Long memberId, @Nullable GroupRoomStatus roomStatus, Pageable pageable) {
        List<GroupRoomMember> content = jpaQueryFactory
                .selectFrom(groupRoomMember)
                .join(groupRoomMember.room, groupRoom).fetchJoin()
                .where(myActiveMembershipPredicate(memberId, roomStatus))
                .orderBy(groupRoom.createdAt.desc(), groupRoom.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(groupRoomMember.count())
                .from(groupRoomMember)
                .join(groupRoomMember.room, groupRoom)
                .where(myActiveMembershipPredicate(memberId, roomStatus));

        return PageableExecutionUtils.getPage(content, pageable, () -> nullableCount(countQuery.fetchOne()));
    }

    @Override
    public boolean existsActiveMembershipInNotDeletedRoom(Long roomId, Long memberId) {
        return jpaQueryFactory
                .selectOne()
                .from(groupRoomMember)
                .join(groupRoomMember.room, groupRoom)
                .where(activeMembershipPredicate(roomId, memberId))
                .fetchFirst() != null;
    }

    @Override
    public Optional<GroupRoomMember> findActiveMembershipInNotDeletedRoom(Long roomId, Long memberId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(groupRoomMember)
                        .join(groupRoomMember.room, groupRoom)
                        .where(activeMembershipPredicate(roomId, memberId))
                        .fetchOne()
        );
    }

    @Override
    public List<GroupRoomMember> findActiveMembersByRoomId(Long roomId) {
        return jpaQueryFactory
                .selectFrom(groupRoomMember)
                .join(groupRoomMember.member, member).fetchJoin()
                .where(
                        groupRoomMember.room.id.eq(roomId),
                        groupRoomMember.status.eq(GroupMemberStatus.ACTIVE),
                        member.status.eq(MemberStatus.ACTIVE)
                )
                .orderBy(
                        new CaseBuilder()
                                .when(groupRoomMember.role.eq(GroupMemberRole.OWNER))
                                .then(0)
                                .otherwise(1)
                                .asc(),
                        groupRoomMember.joinedAt.asc(),
                        groupRoomMember.id.asc()
                )
                .fetch();
    }

    @Override
    public List<GroupRoomMember> findActiveMembershipsInOtherRooms(Long memberId) {
        return jpaQueryFactory
                .selectFrom(groupRoomMember)
                .join(groupRoomMember.room, groupRoom).fetchJoin()
                .where(
                        groupRoomMember.member.id.eq(memberId),
                        groupRoomMember.status.eq(GroupMemberStatus.ACTIVE),
                        groupRoom.status.ne(GroupRoomStatus.DELETED),
                        groupRoom.hostMember.id.ne(memberId)
                )
                .orderBy(groupRoom.id.asc())
                .fetch();
    }

    private BooleanExpression[] myActiveMembershipPredicate(Long memberId, @Nullable GroupRoomStatus roomStatus) {
        return new BooleanExpression[]{
                groupRoomMember.member.id.eq(memberId),
                groupRoomMember.status.eq(GroupMemberStatus.ACTIVE),
                groupRoom.status.ne(GroupRoomStatus.DELETED),
                roomStatus == null ? null : groupRoom.status.eq(roomStatus)
        };
    }

    private BooleanExpression[] activeMembershipPredicate(Long roomId, Long memberId) {
        return new BooleanExpression[]{
                groupRoom.id.eq(roomId),
                groupRoom.status.ne(GroupRoomStatus.DELETED),
                groupRoomMember.member.id.eq(memberId),
                groupRoomMember.status.eq(GroupMemberStatus.ACTIVE)
        };
    }

    private long nullableCount(@Nullable Long count) {
        return count == null ? 0L : count;
    }
}
