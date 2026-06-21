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
    List<PersonalRecommendation> findByMemberId(Long memberId);

    List<PersonalRecommendation> findByMemberIdOrderByRequestedAtDescIdDesc(Long memberId);

    Optional<PersonalRecommendation> findByIdAndMemberId(Long id, Long memberId);

    Optional<PersonalRecommendation> findFirstByMemberIdAndStatusAndSelectedCandidateIsNullAndClosedAtIsNullOrderByRequestedAtDescIdDesc(
            long memberId,
            PersonalRecommendationStatus status
    );

    @Query("""
            select recommendation
            from PersonalRecommendation recommendation
            where recommendation.member.id = :memberId
              and (
                    recommendation.status <> :openStatus
                    or recommendation.requestedAt > :activeThreshold
              )
            order by recommendation.requestedAt desc, recommendation.id desc
            """)
    Page<PersonalRecommendation> findVisibleByMemberIdOrderByRequestedAtDescIdDesc(
            @Param("memberId") long memberId,
            @Param("openStatus") PersonalRecommendationStatus openStatus,
            @Param("activeThreshold") LocalDateTime activeThreshold,
            Pageable pageable
    );
}
