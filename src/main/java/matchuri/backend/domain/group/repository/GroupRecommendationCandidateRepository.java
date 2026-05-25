package matchuri.backend.domain.group.repository;

import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendationCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRecommendationCandidateRepository extends JpaRepository<GroupRecommendationCandidate, Long> {

    List<GroupRecommendationCandidate> findAllByGroupRecommendationIdOrderByRankNoAsc(Long recommendationId);
}
