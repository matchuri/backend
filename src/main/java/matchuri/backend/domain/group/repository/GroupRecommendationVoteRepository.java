package matchuri.backend.domain.group.repository;

import java.util.Collection;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupRecommendationVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRecommendationVoteRepository extends JpaRepository<GroupRecommendationVote, Long> {

    long countByGroupRecommendationId(Long groupRecommendationId);

    @Query("""
            select vote.candidate.id as candidateId, count(vote) as voteCount
            from GroupRecommendationVote vote
            where vote.candidate.id in :candidateIds
            group by vote.candidate.id
            """)
    List<GroupRecommendationVoteCountProjection> countVotesByCandidateIds(
            @Param("candidateIds") Collection<Long> candidateIds
    );
}
