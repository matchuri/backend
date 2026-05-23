package matchuri.backend.domain.group.repository;

import matchuri.backend.domain.group.entity.GroupRecommendationCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRecommendationCandidateRepository extends JpaRepository<GroupRecommendationCandidate, Long> {
}
