package matchuri.backend.domain.group.support.room;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.exception.GroupErrorCode;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupRoomReader {

    private final GroupRoomRepository groupRoomRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;

    public GroupRoom getActiveGroupRoom(Long groupId) {
        GroupRoom room = groupRoomRepository.findByIdAndStatusNot(groupId, GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, groupId));

        if (!room.isActive()) {
            throw new BusinessException(GroupErrorCode.NOT_ACTIVE, room.getId());
        }

        return room;
    }

    public GroupRoomMember getActiveMembership(Long groupId, Long memberId) {
        return groupRoomMemberRepository.findActiveMembershipInNotDeletedRoom(groupId, memberId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.ACCESS_DENIED, groupId));
    }
}


