package matchuri.backend.domain.member.repository.impl;

import static matchuri.backend.domain.image.entity.QImageAsset.imageAsset;
import static matchuri.backend.domain.member.entity.QMember.member;
import static matchuri.backend.domain.member.entity.QMemberLocation.memberLocation;
import static matchuri.backend.domain.member.entity.QMemberProfileImage.memberProfileImage;
import static matchuri.backend.domain.member.entity.QMemberTasteProfile.memberTasteProfile;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberHomeRow;
import matchuri.backend.domain.member.repository.MemberRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Member> findByActiveMemberByNickname(String nickname) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(member)
                        .from(member)
                        .where(
                                member.nickname.eq(nickname),
                                member.status.eq(MemberStatus.ACTIVE)
                        )
                        .fetchOne()
        );
    }

    @Override
    public Optional<MemberHomeRow> findHomeRowByMemberId(Long memberId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(Projections.constructor(
                                MemberHomeRow.class,
                                member.id,
                                member.loginId,
                                member.nickname,
                                member.social,
                                member.email,
                                imageAsset.objectKey,
                                memberLocation.latitude,
                                memberLocation.longitude,
                                memberLocation.radiusMeters,
                                memberLocation.address,
                                memberTasteProfile.id,
                                memberTasteProfile.profileVersion,
                                memberTasteProfile.updatedAt
                        ))
                        .from(member)
                        .leftJoin(memberProfileImage).on(memberProfileImage.member.eq(member))
                        .leftJoin(memberProfileImage.imageAsset, imageAsset)
                        .leftJoin(memberLocation).on(memberLocation.member.eq(member))
                        .leftJoin(memberTasteProfile).on(memberTasteProfile.member.eq(member))
                        .where(member.id.eq(memberId))
                        .fetchOne()
        );
    }

    @Override
    public List<Long> findPurgeCandidateIds(MemberStatus status, LocalDateTime now, Pageable pageable) {
        return jpaQueryFactory
                .select(member.id)
                .from(member)
                .where(
                        member.status.eq(status),
                        member.purgeAt.loe(now)
                )
                .orderBy(member.purgeAt.asc(), member.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public Optional<Member> findByIdForUpdate(Long memberId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(member)
                        .where(member.id.eq(memberId))
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .fetchOne()
        );
    }
}
