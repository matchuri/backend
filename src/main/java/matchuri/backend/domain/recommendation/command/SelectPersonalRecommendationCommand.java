package matchuri.backend.domain.recommendation.command;

import java.math.BigDecimal;

public record SelectPersonalRecommendationCommand(
        Long personalRecommendationId,
        Long selectedCandidateId,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters,
        String address
) {
}
