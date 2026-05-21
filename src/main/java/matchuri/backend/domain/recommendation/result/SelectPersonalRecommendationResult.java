package matchuri.backend.domain.recommendation.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCandidate;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCloseReason;

public record SelectPersonalRecommendationResult(
        Long id,
        Long selectedCandidateId,
        LocalDateTime closedAt,
        PersonalRecommendationCloseReason closeReason,
        LocalDateTime updatedAt
) {
    public static SelectPersonalRecommendationResult of(
            PersonalRecommendation personalRecommendation,
            PersonalRecommendationCandidate selectedCandidate
    ) {
        return new SelectPersonalRecommendationResult(
                personalRecommendation.getId(),
                selectedCandidate.getId(),
                personalRecommendation.getClosedAt(),
                personalRecommendation.getCloseReason(),
                personalRecommendation.getUpdatedAt()
        );
    }
}
