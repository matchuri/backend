package matchuri.backend.domain.recommendation.repository.impl;

import static matchuri.backend.domain.menu.entity.QMenuItem.menuItem;
import static matchuri.backend.domain.recommendation.entity.QPersonalRecommendation.personalRecommendation;
import static matchuri.backend.domain.recommendation.entity.QPersonalRecommendationCandidate.personalRecommendationCandidate;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;
import matchuri.backend.domain.recommendation.repository.PersonalRecommendationRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PersonalRecommendationRepositoryImpl implements PersonalRecommendationRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<PersonalRecommendation> findRecentSelectedByMemberId(Long memberId, Pageable pageable) {
        return jpaQueryFactory
                .selectFrom(personalRecommendation)
                .join(personalRecommendation.selectedCandidate, personalRecommendationCandidate).fetchJoin()
                .join(personalRecommendationCandidate.menuItem, menuItem).fetchJoin()
                .where(
                        personalRecommendation.member.id.eq(memberId),
                        personalRecommendation.status.eq(PersonalRecommendationStatus.SELECTED)
                )
                .orderBy(personalRecommendation.requestedAt.desc(), personalRecommendation.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
