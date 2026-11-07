package matchuri.backend.domain.group.command;

import java.math.BigDecimal;

public record FinalizeGroupRecommendationCommand(
        Long groupId,
        Long sessionId,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters,
        String address
) {
}
