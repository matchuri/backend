package matchuri.backend.domain.member.repository.impl;

import static matchuri.backend.domain.image.entity.QImageAsset.imageAsset;
import static matchuri.backend.domain.member.entity.QMember.member;
import static matchuri.backend.domain.member.entity.QMemberLocation.memberLocation;
import static matchuri.backend.domain.member.entity.QMemberProfileImage.memberProfileImage;
import static matchuri.backend.domain.member.entity.QMemberTasteProfile.memberTasteProfile;
import static matchuri.backend.domain.member.entity.QMemberTasteProfileCategory.memberTasteProfileCategory;
import static matchuri.backend.domain.menu.entity.QAttributeCategory.attributeCategory;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberHomeQueryResult;
import matchuri.backend.domain.member.repository.MemberHomeQueryResult.AttributeCategoryRow;
import matchuri.backend.domain.member.repository.MemberRepositoryCustom;
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
    public Optional<MemberHomeQueryResult> findHomeQueryResultByMemberId(Long memberId) {
        List<Tuple> rows = jpaQueryFactory
                .select(
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
                        memberTasteProfile.profileVersion,
                        memberTasteProfile.updatedAt,
                        attributeCategory.id,
                        attributeCategory.categoryType,
                        attributeCategory.code,
                        attributeCategory.name,
                        attributeCategory.sortOrder
                )
                .from(member)
                .leftJoin(memberProfileImage).on(memberProfileImage.member.eq(member))
                .leftJoin(memberProfileImage.imageAsset, imageAsset)
                .leftJoin(memberLocation).on(memberLocation.member.eq(member))
                .leftJoin(memberTasteProfile).on(memberTasteProfile.member.eq(member))
                .leftJoin(memberTasteProfileCategory).on(memberTasteProfileCategory.profile.eq(memberTasteProfile))
                .leftJoin(memberTasteProfileCategory.attributeCategory, attributeCategory)
                .where(member.id.eq(memberId))
                .orderBy(
                        attributeCategory.categoryType.asc(),
                        attributeCategory.sortOrder.asc(),
                        attributeCategory.id.asc()
                )
                .fetch();

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Tuple firstRow = rows.getFirst();
        List<AttributeCategoryRow> attributeCategories = rows.stream()
                .filter(row -> row.get(attributeCategory.id) != null)
                .map(row -> new AttributeCategoryRow(
                        row.get(attributeCategory.id),
                        row.get(attributeCategory.categoryType),
                        row.get(attributeCategory.code),
                        row.get(attributeCategory.name),
                        row.get(attributeCategory.sortOrder)
                ))
                .toList();

        return Optional.of(new MemberHomeQueryResult(
                firstRow.get(member.id),
                firstRow.get(member.loginId),
                firstRow.get(member.nickname),
                firstRow.get(member.social),
                firstRow.get(member.email),
                firstRow.get(imageAsset.objectKey),
                firstRow.get(memberLocation.latitude),
                firstRow.get(memberLocation.longitude),
                firstRow.get(memberLocation.radiusMeters),
                firstRow.get(memberLocation.address),
                firstRow.get(memberTasteProfile.profileVersion),
                firstRow.get(memberTasteProfile.updatedAt),
                attributeCategories
        ));
    }
}
