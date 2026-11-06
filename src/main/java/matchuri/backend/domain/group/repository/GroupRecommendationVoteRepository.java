package matchuri.backend.domain.group.repository;

import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupRecommendationVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRecommendationVoteRepository extends JpaRepository<GroupRecommendationVote, Long>,
        GroupRecommendationVoteRepositoryCustom {

    long countByGroupRecommendationId(Long recommendationId);

    boolean existsByGroupRecommendationIdAndMemberId(Long recommendationId, Long memberId);

    List<GroupRecommendationVote> findAllByGroupRecommendationId(Long recommendationId);

    Optional<GroupRecommendationVote> findByGroupRecommendationIdAndMemberId(Long recommendationId, Long memberId);

    long deleteByGroupRecommendationIdAndMemberId(Long recommendationId, Long memberId);
}
