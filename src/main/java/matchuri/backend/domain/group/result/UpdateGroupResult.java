package matchuri.backend.domain.group.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRoomStatus;

public record UpdateGroupResult(
        Long groupId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters,
        String address,
        GroupRoomStatus status,
        LocalDateTime updatedAt
) {
}
