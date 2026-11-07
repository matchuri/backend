package matchuri.backend.domain.member.repository.impl;

import static matchuri.backend.domain.member.entity.QMemberTasteProfileCategory.memberTasteProfileCategory;
import static matchuri.backend.domain.menu.entity.QAttributeCategory.attributeCategory;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.member.entity.MemberTasteProfileCategory;
import matchuri.backend.domain.member.repository.MemberTasteProfileAttributeCategoryRow;
import matchuri.backend.domain.member.repository.MemberTasteProfileCategoryRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberTasteProfileCategoryRepositoryImpl implements MemberTasteProfileCategoryRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MemberTasteProfileCategory> findAllByProfileIdOrderByDisplay(Long profileId) {
        return jpaQueryFactory
                .selectFrom(memberTasteProfileCategory)
                .join(memberTasteProfileCategory.attributeCategory, attributeCategory).fetchJoin()
                .where(memberTasteProfileCategory.profile.id.eq(profileId))
                .orderBy(
                        attributeCategory.categoryType.asc(),
                        attributeCategory.sortOrder.asc(),
                        attributeCategory.id.asc()
                )
                .fetch();
    }

    @Override
    public List<MemberTasteProfileAttributeCategoryRow> findAttributeCategoryRowsByProfileId(Long profileId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        MemberTasteProfileAttributeCategoryRow.class,
                        attributeCategory.id,
                        attributeCategory.categoryType,
                        attributeCategory.code,
                        attributeCategory.name,
                        attributeCategory.sortOrder
                ))
                .from(memberTasteProfileCategory)
                .join(memberTasteProfileCategory.attributeCategory, attributeCategory)
                .where(memberTasteProfileCategory.profile.id.eq(profileId))
                .orderBy(
                        attributeCategory.categoryType.asc(),
                        attributeCategory.sortOrder.asc(),
                        attributeCategory.id.asc()
                )
                .fetch();
    }
}
