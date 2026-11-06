package matchuri.backend.domain.group.repository;

import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRoomMemberRepository extends JpaRepository<GroupRoomMember, Long>,
        GroupRoomMemberRepositoryCustom {

    Optional<GroupRoomMember> findByRoomIdAndMemberId(Long roomId, Long memberId);
}
