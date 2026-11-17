package matchuri.backend.domain.auth.repository.impl;

import static matchuri.backend.domain.auth.entity.QOAuthUnlinkJob.oAuthUnlinkJob;
import static matchuri.backend.domain.member.entity.QMember.member;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.entity.Status;
import matchuri.backend.domain.auth.repository.OAuthUnlinkJobRepositoryCustom;
import matchuri.backend.domain.auth.repository.OAuthUnlinkRetryRow;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OAuthUnlinkJobRepositoryImpl implements OAuthUnlinkJobRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<OAuthUnlinkRetryRow> findDueJobs(Status status, LocalDateTime now, Pageable pageable) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        OAuthUnlinkRetryRow.class,
                        oAuthUnlinkJob.id,
                        member.id,
                        member.purgeAt
                ))
                .from(oAuthUnlinkJob)
                .join(oAuthUnlinkJob.member, member)
                .where(
                        oAuthUnlinkJob.status.eq(status),
                        oAuthUnlinkJob.nextAttemptAt.loe(now),
                        member.purgeAt.gt(now)
                )
                .orderBy(oAuthUnlinkJob.nextAttemptAt.asc(), oAuthUnlinkJob.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
