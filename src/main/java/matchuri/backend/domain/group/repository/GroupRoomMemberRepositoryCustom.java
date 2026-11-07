package matchuri.backend.domain.group.repository;

import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupMemberStatus;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupRoomMemberRepositoryCustom {

    List<GroupRoomMemberCountRow> countMembersByRoomIdsAndStatus(List<Long> roomIds, GroupMemberStatus memberStatus);

    Page<GroupRoomMember> findMyActiveMemberships(Long memberId, @Nullable GroupRoomStatus roomStatus, Pageable pageable);

    boolean existsActiveMembershipInNotDeletedRoom(Long roomId, Long memberId);

    Optional<GroupRoomMember> findActiveMembershipInNotDeletedRoom(Long roomId, Long memberId);

    List<GroupRoomMember> findActiveMembersByRoomId(Long roomId);

    List<GroupRoomMember> findActiveMembershipsInOtherRooms(Long memberId);
}
