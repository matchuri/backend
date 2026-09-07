package matchuri.backend.domain.recommendation.repository;

import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCandidate;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@NullMarked
public interface PersonalRecommendationCandidateRepository extends JpaRepository<PersonalRecommendationCandidate, Long>,
        PersonalRecommendationCandidateRepositoryCustom {

    List<PersonalRecommendationCandidate> findByPersonalRecommendationIdOrderByRankNoAsc(Long personalRecommendationId);

    Optional<PersonalRecommendationCandidate> findByIdAndPersonalRecommendationId(
            Long id,
            Long personalRecommendationId
    );

    @Query("""
            select candidate
            from PersonalRecommendationCandidate candidate
            join fetch candidate.menuItem menuItem
            join candidate.personalRecommendation recommendation
            left join recommendation.selectedCandidate selectedCandidate
            where recommendation.id in :personalRecommendationIds
              and (
                selectedCandidate.id = candidate.id
                or (selectedCandidate.id is null and candidate.rankNo = 1)
              )
            """)
    List<PersonalRecommendationCandidate> findRepresentativeCandidates(
            @Param("personalRecommendationIds") List<Long> personalRecommendationIds
    );
}
