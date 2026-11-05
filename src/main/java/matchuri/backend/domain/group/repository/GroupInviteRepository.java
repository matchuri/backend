package matchuri.backend.domain.group.repository;

import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupInvite;
import matchuri.backend.domain.group.entity.GroupInviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupInviteRepository extends JpaRepository<GroupInvite, Long>, GroupInviteRepositoryCustom {

    List<GroupInvite> findAllByRoomIdAndStatus(Long roomId, GroupInviteStatus status);

    boolean existsByRoomIdAndTargetMemberIdAndStatus(Long roomId, Long targetMemberId, GroupInviteStatus status);

    boolean existsByTargetMemberIdAndStatusAndExpiresAtAfter(
            Long targetMemberId,
            GroupInviteStatus status,
            LocalDateTime now
    );
}
