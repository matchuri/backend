package matchuri.backend.domain.recommendation.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface PersonalRecommendationRepository extends JpaRepository<PersonalRecommendation, Long> {
    List<PersonalRecommendation> findByMemberId(Long memberId);

    List<PersonalRecommendation> findByMemberIdOrderByRequestedAtDescIdDesc(Long memberId);
    Page<PersonalRecommendation> findByMemberIdOrderByRequestedAtDescIdDesc(Long memberId, Pageable pageable);

    Optional<PersonalRecommendation> findByIdAndMemberId(Long id, Long memberId);

    List<PersonalRecommendation> findByStatusAndSelectedCandidateIsNullAndClosedAtIsNullAndRequestedAtLessThanEqual(
            PersonalRecommendationStatus status,
            LocalDateTime requestedAt
    );

    Optional<PersonalRecommendation> findFirstByMemberIdAndStatusAndSelectedCandidateIsNullAndClosedAtIsNullOrderByRequestedAtDescIdDesc(
            long memberId,
            PersonalRecommendationStatus status
    );
}
