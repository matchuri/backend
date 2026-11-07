package matchuri.backend.domain.member.repository.impl;

import static matchuri.backend.domain.member.entity.QMemberTasteProfileRestrictionIngredient.memberTasteProfileRestrictionIngredient;
import static matchuri.backend.domain.menu.entity.QIngredient.ingredient;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.member.entity.MemberTasteProfileRestrictionIngredient;
import matchuri.backend.domain.member.repository.MemberTasteProfileRestrictionIngredientRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberTasteProfileRestrictionIngredientRepositoryImpl
        implements MemberTasteProfileRestrictionIngredientRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MemberTasteProfileRestrictionIngredient> findAllByProfileIdOrderByDisplay(Long profileId) {
        return jpaQueryFactory
                .selectFrom(memberTasteProfileRestrictionIngredient)
                .join(memberTasteProfileRestrictionIngredient.ingredient, ingredient).fetchJoin()
                .where(memberTasteProfileRestrictionIngredient.profile.id.eq(profileId))
                .orderBy(ingredient.sortOrder.asc(), ingredient.id.asc())
                .fetch();
    }
}
