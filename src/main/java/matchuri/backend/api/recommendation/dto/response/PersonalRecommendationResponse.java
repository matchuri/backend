package matchuri.backend.api.recommendation.dto.response;

import java.time.LocalDateTime;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;

public record PersonalRecommendationResponse(
        long id,
        PersonalRecommendationStatus status,
        LocalDateTime requestedAt
) {
    public static PersonalRecommendationResponse mock() {
        return new PersonalRecommendationResponse(1, PersonalRecommendationStatus.REQUESTED, LocalDateTime.now());
    }
}
