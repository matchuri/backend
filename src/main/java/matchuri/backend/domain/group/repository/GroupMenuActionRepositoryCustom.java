package matchuri.backend.domain.group.repository;

import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupMenuActionType;

public interface GroupMenuActionRepositoryCustom {

    List<Long> findMenuItemIdsByGroupRoomIdAndActionTypeAndCreatedAtAfter(Long groupRoomId, GroupMenuActionType actionType, LocalDateTime createdAt);
}
