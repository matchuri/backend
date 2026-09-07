package matchuri.backend.domain.recommendation.repository.impl;

import static matchuri.backend.domain.image.entity.QImageAsset.imageAsset;
import static matchuri.backend.domain.menu.entity.QMenuItem.menuItem;
import static matchuri.backend.domain.menu.entity.QMenuItemImage.menuItemImage;
import static matchuri.backend.domain.recommendation.entity.QPersonalRecommendationCandidate.personalRecommendationCandidate;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.recommendation.repository.PersonalRecommendationCandidateQueryRow;
import matchuri.backend.domain.recommendation.repository.PersonalRecommendationCandidateRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PersonalRecommendationCandidateRepositoryImpl implements PersonalRecommendationCandidateRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<PersonalRecommendationCandidateQueryRow> findCandidateRowsByPersonalRecommendationId(
            Long personalRecommendationId
    ) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        PersonalRecommendationCandidateQueryRow.class,
                        personalRecommendationCandidate.id,
                        menuItem.id,
                        menuItem.name,
                        imageAsset.objectKey,
                        personalRecommendationCandidate.rankNo,
                        personalRecommendationCandidate.score
                ))
                .from(personalRecommendationCandidate)
                .join(personalRecommendationCandidate.menuItem, menuItem)
                .leftJoin(menuItemImage).on(menuItemImage.menu.eq(menuItem))
                .leftJoin(menuItemImage.imageAsset, imageAsset)
                .where(personalRecommendationCandidate.personalRecommendation.id.eq(personalRecommendationId))
                .orderBy(personalRecommendationCandidate.rankNo.asc())
                .fetch();
    }
}
