package matchuri.backend.domain.group.repository.impl;

import static matchuri.backend.domain.group.entity.QGroupInvite.groupInvite;
import static matchuri.backend.domain.group.entity.QGroupRoom.groupRoom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.entity.GroupInvite;
import matchuri.backend.domain.group.entity.GroupInviteStatus;
import matchuri.backend.domain.group.repository.GroupInviteRepositoryCustom;
import matchuri.backend.domain.member.entity.QMember;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupInviteRepositoryImpl implements GroupInviteRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<GroupInvite> findMyInvites(Long targetMemberId, @Nullable GroupInviteStatus status, LocalDateTime now, Pageable pageable) {
        QMember requestMember = new QMember("requestMember");
        List<GroupInvite> content = jpaQueryFactory
                .selectFrom(groupInvite)
                .join(groupInvite.room, groupRoom).fetchJoin()
                .join(groupInvite.requestMember, requestMember).fetchJoin()
                .where(myInvitePredicate(targetMemberId, status, now))
                .orderBy(groupInvite.createdAt.desc(), groupInvite.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(groupInvite.count())
                .from(groupInvite)
                .where(myInvitePredicate(targetMemberId, status, now));

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> nullableCount(countQuery.fetchOne())
        );
    }

    private BooleanExpression[] myInvitePredicate(Long targetMemberId, @Nullable GroupInviteStatus status, LocalDateTime now) {
        return new BooleanExpression[]{
                groupInvite.targetMember.id.eq(targetMemberId),
                status == null ? null : groupInvite.status.eq(status),
                groupInvite.expiresAt.gt(now)
        };
    }

    private long nullableCount(@Nullable Long count) {
        return count == null ? 0L : count;
    }
}
