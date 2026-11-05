package matchuri.backend.domain.group.repository;

import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomStatus;

public interface GroupRoomRepositoryCustom {

    Optional<GroupRoom> findByIdAndStatusNotForUpdate(Long id, GroupRoomStatus status);

    List<GroupRoom> findOwnedNotDeletedForUpdate(Long memberId);
}
