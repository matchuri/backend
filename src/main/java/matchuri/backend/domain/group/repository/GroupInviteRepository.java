package matchuri.backend.domain.group.repository;

import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupInviteRepository extends JpaRepository<GroupInvite, Long> {

    boolean existsByInviteCode(String inviteCode);

    @Query("""
            select groupInvite
            from GroupInvite groupInvite
            join fetch groupInvite.room room
            where groupInvite.inviteCode = :inviteCode
            """)
    Optional<GroupInvite> findByInviteCodeWithRoom(@Param("inviteCode") String inviteCode);
}
