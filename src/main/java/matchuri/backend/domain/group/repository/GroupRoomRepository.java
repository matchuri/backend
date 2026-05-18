package matchuri.backend.domain.group.repository;

import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRoomRepository extends JpaRepository<GroupRoom, Long> {

    Optional<GroupRoom> findByIdAndStatusNot(Long id, GroupRoomStatus status);

    boolean existsByInviteCode(String inviteCode);
}
