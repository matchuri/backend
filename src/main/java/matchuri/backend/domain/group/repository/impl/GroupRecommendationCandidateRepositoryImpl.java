package matchuri.backend.domain.group.repository.impl;

import static matchuri.backend.domain.group.entity.QGroupRecommendationCandidate.groupRecommendationCandidate;
import static matchuri.backend.domain.group.entity.QGroupRecommendationVote.groupRecommendationVote;
import static matchuri.backend.domain.image.entity.QImageAsset.imageAsset;
import static matchuri.backend.domain.menu.entity.QMenuItem.menuItem;
import static matchuri.backend.domain.menu.entity.QMenuItemImage.menuItemImage;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.repository.GroupRecommendationCandidateQueryRow;
import matchuri.backend.domain.group.repository.GroupRecommendationCandidateRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupRecommendationCandidateRepositoryImpl implements GroupRecommendationCandidateRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<GroupRecommendationCandidateQueryRow> findCandidateRowsWithVoteCounts(Long recommendationId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        GroupRecommendationCandidateQueryRow.class,
                        groupRecommendationCandidate.id,
                        menuItem.id,
                        menuItem.name,
                        imageAsset.objectKey,
                        groupRecommendationCandidate.rankNo,
                        groupRecommendationCandidate.score,
                        groupRecommendationVote.id.count()
                ))
                .from(groupRecommendationCandidate)
                .join(groupRecommendationCandidate.menuItem, menuItem)
                .leftJoin(menuItemImage).on(menuItemImage.menu.eq(menuItem))
                .leftJoin(menuItemImage.imageAsset, imageAsset)
                .leftJoin(groupRecommendationVote)
                .on(groupRecommendationVote.candidate.eq(groupRecommendationCandidate))
                .where(groupRecommendationCandidate.groupRecommendation.id.eq(recommendationId))
                .groupBy(
                        groupRecommendationCandidate.id,
                        menuItem.id,
                        menuItem.name,
                        imageAsset.objectKey,
                        groupRecommendationCandidate.rankNo,
                        groupRecommendationCandidate.score
                )
                .orderBy(groupRecommendationCandidate.rankNo.asc())
                .fetch();
    }
}
