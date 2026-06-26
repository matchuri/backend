package matchuri.backend.domain.group.repository;

import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupRecommendationVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRecommendationVoteRepository extends JpaRepository<GroupRecommendationVote, Long> {

    @Query("""
            select vote.candidate.id as candidateId, count(vote.id) as voteCount
            from GroupRecommendationVote vote
            where vote.groupRecommendation.id = :recommendationId
            group by vote.candidate.id
            """)
    List<GroupCandidateVoteCountProjection> countVotesByCandidateId(
            @Param("recommendationId") Long recommendationId
    );

    long countByGroupRecommendationId(Long recommendationId);

    boolean existsByGroupRecommendationIdAndMemberId(Long recommendationId, Long memberId);

    List<GroupRecommendationVote> findAllByGroupRecommendationId(Long recommendationId);

    Optional<GroupRecommendationVote> findByGroupRecommendationIdAndMemberId(Long recommendationId, Long memberId);
}
