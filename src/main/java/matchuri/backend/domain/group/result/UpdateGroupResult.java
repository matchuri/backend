package matchuri.backend.domain.group.result;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupRoomStatus;

public record UpdateGroupResult(
        Long groupId,
        String name,
        GroupRoomStatus status,
        LocalDateTime updatedAt
) {
}
