package matchuri.backend.domain.group.command;

import java.math.BigDecimal;

public record CreateGroupRecommendationCommand(
        Long groupId,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters,
        String address
) {
}
