package matchuri.backend.domain.recommendation.repository;

import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCandidate;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface PersonalRecommendationCandidateRepository extends JpaRepository<PersonalRecommendationCandidate, Long> {
}
