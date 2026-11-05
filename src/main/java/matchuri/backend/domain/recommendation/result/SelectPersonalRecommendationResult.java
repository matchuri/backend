package matchuri.backend.domain.recommendation.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCandidate;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;

public record SelectPersonalRecommendationResult(
        Long id,
        PersonalRecommendationStatus status,
        Long selectedCandidateId,
        LocalDateTime closedAt,
        LocalDateTime updatedAt
) {
    public static SelectPersonalRecommendationResult of(
            PersonalRecommendation personalRecommendation,
            PersonalRecommendationCandidate selectedCandidate
    ) {
        return new SelectPersonalRecommendationResult(
                personalRecommendation.getId(),
                personalRecommendation.getStatus(),
                selectedCandidate.getId(),
                personalRecommendation.getClosedAt(),
                personalRecommendation.getUpdatedAt()
        );
    }
}
