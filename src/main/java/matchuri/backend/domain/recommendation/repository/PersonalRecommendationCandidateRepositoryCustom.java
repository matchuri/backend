package matchuri.backend.domain.recommendation.repository;

import java.util.List;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCandidate;

public interface PersonalRecommendationCandidateRepositoryCustom {

    List<PersonalRecommendationCandidateQueryRow> findCandidateRowsByPersonalRecommendationId(Long personalRecommendationId);

    List<PersonalRecommendationCandidate> findRepresentativeCandidates(List<Long> personalRecommendationIds);
}
