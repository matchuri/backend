package matchuri.backend.domain.member.command;

import java.math.BigDecimal;

public record PutMemberLocationCommand(
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters,
        String address
) {
}
