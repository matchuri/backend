package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRoomStatus;

public record DeleteGroupResult(
        Long groupId,
        GroupRoomStatus status,
        LocalDateTime deletedAt
) {
}
