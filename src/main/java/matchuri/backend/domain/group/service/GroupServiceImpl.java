package matchuri.backend.domain.group.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.command.CreateGroupCommand;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.group.result.CreateGroupResult;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.support.member.ActiveMemberReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final ActiveMemberReader activeMemberReader;
    private final GroupRoomRepository groupRoomRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;

    @Override
    public CreateGroupResult createGroup(CreateGroupCommand command) {
        Member hostMember = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom groupRoom = new GroupRoom(
                command.name(),
                hostMember,
                command.latitude(),
                command.longitude()
        );
        GroupRoom savedGroupRoom = groupRoomRepository.save(groupRoom);
        GroupRoomMember ownerMember = new GroupRoomMember(
                savedGroupRoom,
                hostMember,
                GroupMemberRole.OWNER,
                LocalDateTime.now()
        );
        groupRoomMemberRepository.save(ownerMember);

        return new CreateGroupResult(savedGroupRoom.getId(), savedGroupRoom.getStatus());
    }
}
