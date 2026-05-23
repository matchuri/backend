package matchuri.backend.api.recommendation.dto.response;

import java.time.LocalDateTime;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;

public record PersonalRecommendationResponse(
        long id,
        PersonalRecommendationStatus status,
        LocalDateTime requestedAt,
        LocalDateTime closedAt
) {
    public static PersonalRecommendationResponse mock() {
        return new PersonalRecommendationResponse(
                9001L,
                PersonalRecommendationStatus.OPEN,
                LocalDateTime.of(2026, 5, 6, 12, 10),
                null
        );
    }
}
