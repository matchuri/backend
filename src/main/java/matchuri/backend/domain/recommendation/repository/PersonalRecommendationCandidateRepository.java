package matchuri.backend.domain.recommendation.repository;

import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCandidate;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface PersonalRecommendationCandidateRepository extends JpaRepository<PersonalRecommendationCandidate, Long> {

    List<PersonalRecommendationCandidate> findByPersonalRecommendationIdOrderByRankNoAsc(Long personalRecommendationId);

    Optional<PersonalRecommendationCandidate> findByIdAndPersonalRecommendationId(
            Long id,
            Long personalRecommendationId
    );
}
