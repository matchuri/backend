package matchuri.backend.domain.recommendation.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@NullMarked
public interface PersonalRecommendationRepository extends JpaRepository<PersonalRecommendation, Long> {

    Optional<PersonalRecommendation> findFirstByMemberIdOrderByRequestedAtDescIdDesc(Long memberId);

    @Query("""
            select recommendation from PersonalRecommendation recommendation
            join fetch recommendation.selectedCandidate candidate
            join fetch candidate.menuItem
            where recommendation.member.id = :memberId
              and recommendation.status = matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus.SELECTED
            order by recommendation.requestedAt desc, recommendation.id desc
            """)
    List<PersonalRecommendation> findRecentSelectedByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    List<PersonalRecommendation> findByMemberId(Long memberId);

    List<PersonalRecommendation> findByMemberIdOrderByRequestedAtDescIdDesc(Long memberId);

    Page<PersonalRecommendation> findByMemberIdOrderByRequestedAtDescIdDesc(Long memberId, Pageable pageable);

    Optional<PersonalRecommendation> findByIdAndMemberId(Long id, Long memberId);

    Optional<PersonalRecommendation> findFirstByMemberIdAndStatusAndSelectedCandidateIsNullAndClosedAtIsNullOrderByRequestedAtDescIdDesc(
            long memberId,
            PersonalRecommendationStatus status
    );

    List<PersonalRecommendation> findByMemberIdAndStatusAndSelectedCandidateIsNullAndClosedAtIsNullAndRequestedAtLessThanEqual(
            long memberId,
            PersonalRecommendationStatus status,
            LocalDateTime requestedAt
    );

    Page<PersonalRecommendation> findByMemberIdAndStatusOrderByRequestedAtDescIdDesc(Long memberId, PersonalRecommendationStatus status, Pageable pageable);
}
