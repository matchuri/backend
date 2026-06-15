package matchuri.backend.domain.group.repository;

import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupLocationRepository extends JpaRepository<GroupLocation, Long> {

    Optional<GroupLocation> findFirstByRoomIdOrderByCreatedAtDescIdDesc(Long roomId);
}
