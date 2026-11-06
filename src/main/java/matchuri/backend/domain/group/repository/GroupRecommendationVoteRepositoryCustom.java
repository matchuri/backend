package matchuri.backend.domain.group.repository;

import java.util.List;

public interface GroupRecommendationVoteRepositoryCustom {

    List<GroupRecommendationVoteQueryRow> findVoteRowsByRecommendationId(Long recommendationId);

    List<GroupCandidateVoteCountRow> countVotesByCandidateId(Long recommendationId);
}
