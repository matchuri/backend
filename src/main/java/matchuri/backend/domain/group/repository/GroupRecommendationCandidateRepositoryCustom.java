package matchuri.backend.domain.group.repository;

import java.util.List;

public interface GroupRecommendationCandidateRepositoryCustom {

    List<GroupRecommendationCandidateQueryRow> findCandidateRowsWithVoteCounts(Long recommendationId);
}
