package matchuri.backend.domain.group.repository.impl;

import static matchuri.backend.domain.group.entity.QGroupRecommendationVote.groupRecommendationVote;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.repository.GroupRecommendationVoteQueryRow;
import matchuri.backend.domain.group.repository.GroupRecommendationVoteRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupRecommendationVoteRepositoryImpl implements GroupRecommendationVoteRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<GroupRecommendationVoteQueryRow> findVoteRowsByRecommendationId(Long recommendationId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        GroupRecommendationVoteQueryRow.class,
                        groupRecommendationVote.member.id,
                        groupRecommendationVote.candidate.id
                ))
                .from(groupRecommendationVote)
                .where(groupRecommendationVote.groupRecommendation.id.eq(recommendationId))
                .fetch();
    }
}
