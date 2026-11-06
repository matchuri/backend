package matchuri.backend.domain.group.repository;

import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendationCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRecommendationCategoryRepository extends JpaRepository<GroupRecommendationCategory, Long> {

    @EntityGraph(attributePaths = "attributeCategory")
    List<GroupRecommendationCategory> findAllByGroupRecommendationIdOrderByRankNoAsc(Long recommendationId);
}
