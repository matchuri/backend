package matchuri.backend.domain.group.repository;

import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupRecommendationReadiness;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRecommendationReadinessRepository
        extends JpaRepository<GroupRecommendationReadiness, Long>,
        GroupRecommendationReadinessRepositoryCustom {

    Optional<GroupRecommendationReadiness> findByGroupRecommendationIdAndMemberId(
            Long groupRecommendationId,
            Long memberId
    );

    List<GroupRecommendationReadiness> findAllByGroupRecommendationId(Long groupRecommendationId);
}
