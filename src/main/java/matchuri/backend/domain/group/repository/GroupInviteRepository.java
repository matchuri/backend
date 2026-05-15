package matchuri.backend.domain.group.repository;

import matchuri.backend.domain.group.entity.GroupInvite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupInviteRepository extends JpaRepository<GroupInvite, Long> {

    boolean existsByInviteCode(String inviteCode);
}
