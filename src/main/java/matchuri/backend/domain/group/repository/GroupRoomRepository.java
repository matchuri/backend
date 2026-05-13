package matchuri.backend.domain.group.repository;

import matchuri.backend.domain.group.entity.GroupRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRoomRepository extends JpaRepository<GroupRoom, Long> {
}
