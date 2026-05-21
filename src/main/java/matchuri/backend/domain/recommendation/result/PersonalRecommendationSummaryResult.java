package matchuri.backend.domain.recommendation.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCloseReason;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;

public record PersonalRecommendationSummaryResult(
        Long id,
        PersonalRecommendationStatus status,
        LocalDateTime requestedAt,
        LocalDateTime closedAt,
        PersonalRecommendationCloseReason closeReason
) {
    public static PersonalRecommendationSummaryResult from(PersonalRecommendation personalRecommendation) {
        return new PersonalRecommendationSummaryResult(
                personalRecommendation.getId(),
                personalRecommendation.getStatus(),
                personalRecommendation.getRequestedAt(),
                personalRecommendation.getClosedAt(),
                personalRecommendation.getCloseReason()
        );
    }
}
