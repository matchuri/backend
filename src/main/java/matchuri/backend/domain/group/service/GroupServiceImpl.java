package matchuri.backend.domain.group.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.command.CreateGroupCommand;
import matchuri.backend.domain.group.command.GetMyGroupsCommand;
import matchuri.backend.domain.group.entity.GroupMemberStatus;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.exception.GroupErrorCode;
import matchuri.backend.domain.group.repository.GroupRoomMemberCountProjection;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.group.result.CreateGroupResult;
import matchuri.backend.domain.group.result.GroupDetailResult;
import matchuri.backend.domain.group.result.GroupMemberSummaryResult;
import matchuri.backend.domain.group.result.GroupSummaryResult;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.support.member.ActiveMemberReader;
import matchuri.backend.global.exception.BusinessException;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

        GroupRoom groupRoom = GroupRoom.createOwnedBy(
                command.name(),
                hostMember,
                command.latitude(),
                command.longitude());

        GroupRoom savedGroupRoom = groupRoomRepository.save(groupRoom);

        return new CreateGroupResult(savedGroupRoom.getId(), savedGroupRoom.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull GroupSummaryResult> getMyGroups(GetMyGroupsCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        Page<GroupRoomMember> memberships = groupRoomMemberRepository.findMyActiveMemberships(
                member.getId(),
                command.status(),
                PageRequest.of(command.page(), command.size())
        );
        Map<Long, Long> activeMemberCounts = countActiveMembers(memberships);

        return memberships.map(membership -> toSummaryResult(membership, activeMemberCounts));
    }

    @Override
    @Transactional(readOnly = true)
    public GroupDetailResult getGroup(Long groupId) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = groupRoomRepository.findByIdAndStatusNot(groupId, GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, groupId));

        if (!groupRoomMemberRepository.existsActiveMembershipInNotDeletedRoom(groupId, member.getId())) {
            throw new BusinessException(GroupErrorCode.ACCESS_DENIED, groupId);
        }

        List<GroupMemberSummaryResult> members = groupRoomMemberRepository.findActiveMembersByRoomId(groupId)
                .stream()
                .map(this::toMemberSummaryResult)
                .toList();

        return new GroupDetailResult(
                room.getId(),
                room.getName(),
                room.getLatitude(),
                room.getLongitude(),
                room.getStatus(),
                members
        );
    }

    private Map<Long, Long> countActiveMembers(Page<GroupRoomMember> memberships) {
        List<Long> roomIds = memberships.getContent().stream()
                .map(membership -> membership.getRoom().getId())
                .toList();

        if (roomIds.isEmpty()) {
            return Map.of();
        }

        return groupRoomMemberRepository.countMembersByRoomIdsAndStatus(roomIds, GroupMemberStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(
                        GroupRoomMemberCountProjection::getRoomId,
                        GroupRoomMemberCountProjection::getMemberCount
                ));
    }

    private GroupSummaryResult toSummaryResult(
            GroupRoomMember membership,
            Map<Long, Long> activeMemberCounts
    ) {
        GroupRoom room = membership.getRoom();

        return new GroupSummaryResult(
                room.getId(),
                room.getName(),
                room.getStatus(),
                activeMemberCounts.getOrDefault(room.getId(), 0L).intValue(),
                null,
                room.getCreatedAt()
        );
    }

    private GroupMemberSummaryResult toMemberSummaryResult(GroupRoomMember membership) {
        Member member = membership.getMember();

        return new GroupMemberSummaryResult(
                member.getId(),
                member.getNickname(),
                membership.getRole(),
                membership.getStatus(),
                membership.getJoinedAt()
        );
    }
}
