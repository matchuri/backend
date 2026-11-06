package matchuri.backend.domain.group.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupInviteLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupInviteLinkRepository extends JpaRepository<GroupInviteLink, Long> {

    Optional<GroupInviteLink> findFirstByRoomIdAndExpiresAtAfterOrderByCreatedAtDescIdDesc(
            Long roomId,
            LocalDateTime now
    );

    List<GroupInviteLink> findAllByRoomIdAndExpiresAtAfter(Long roomId, LocalDateTime now);

    Optional<GroupInviteLink> findByToken(String token);

    boolean existsByToken(String token);
}
