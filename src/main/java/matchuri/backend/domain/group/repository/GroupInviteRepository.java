package matchuri.backend.domain.group.repository;

import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupInvite;
import matchuri.backend.domain.group.entity.GroupInviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupInviteRepository extends JpaRepository<GroupInvite, Long> {

    boolean existsByInviteCode(String inviteCode);

    List<GroupInvite> findAllByRoomIdAndStatus(Long roomId, GroupInviteStatus status);

    @Query("""
            select groupInvite
            from GroupInvite groupInvite
            join fetch groupInvite.room room
            where groupInvite.inviteCode = :inviteCode
            """)
    Optional<GroupInvite> findByInviteCodeWithRoom(@Param("inviteCode") String inviteCode);
}
