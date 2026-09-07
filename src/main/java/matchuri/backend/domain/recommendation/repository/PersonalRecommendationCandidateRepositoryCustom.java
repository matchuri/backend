package matchuri.backend.domain.recommendation.repository;

import java.util.List;

public interface PersonalRecommendationCandidateRepositoryCustom {

    List<PersonalRecommendationCandidateQueryRow> findCandidateRowsByPersonalRecommendationId(Long personalRecommendationId);
}
