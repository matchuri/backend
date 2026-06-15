package matchuri.backend.domain.group.command;

import java.math.BigDecimal;

public record CreateGroupCommand(
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters,
        String address
) {
}
