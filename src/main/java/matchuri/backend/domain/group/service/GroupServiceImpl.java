package matchuri.backend.domain.group.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.command.CreateGroupCommand;
import matchuri.backend.domain.group.command.CreateGroupInviteCommand;
import matchuri.backend.domain.group.command.GetMyGroupsCommand;
import matchuri.backend.domain.group.entity.GroupInvite;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupMemberStatus;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.exception.GroupErrorCode;
import matchuri.backend.domain.group.repository.GroupInviteRepository;
import matchuri.backend.domain.group.repository.GroupRoomMemberCountProjection;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.group.result.CreateGroupInviteResult;
import matchuri.backend.domain.group.result.CreateGroupResult;
import matchuri.backend.domain.group.result.GroupDetailResult;
import matchuri.backend.domain.group.result.GroupMemberSummaryResult;
import matchuri.backend.domain.group.result.GroupSummaryResult;
import matchuri.backend.domain.group.support.GroupInviteCodeGenerator;
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

    private static final int INVITE_EXPIRATION_HOURS = 24;
    private static final int MAX_INVITE_CODE_GENERATION_ATTEMPTS = 5;

    private final ActiveMemberReader activeMemberReader;
    private final GroupRoomRepository groupRoomRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupInviteRepository groupInviteRepository;
    private final GroupInviteCodeGenerator groupInviteCodeGenerator;

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
    public CreateGroupInviteResult createInvite(CreateGroupInviteCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = groupRoomRepository.findByIdAndStatusNot(command.groupId(), GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, command.groupId()));

        if (!room.isActive()) {
            throw new BusinessException(GroupErrorCode.NOT_ACTIVE, command.groupId());
        }

        long memberId = member.getId();
        long hostMemberId = room.getHostMember().getId();

        if (memberId != hostMemberId) {
            throw new BusinessException(GroupErrorCode.ACCESS_DENIED, command.groupId());
        }

        GroupRoomMember membership = room.getGroupRoomHostMember();

        if (!membership.isOwner()) {
            throw new BusinessException(GroupErrorCode.ACCESS_DENIED, command.groupId());
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(INVITE_EXPIRATION_HOURS);
        GroupInvite groupInvite = createUniqueInvite(room, member, expiresAt);

        return new CreateGroupInviteResult(
                room.getId(),
                groupInvite.getInviteCode(),
                groupInvite.getExpiresAt(),
                groupInvite.getStatus()
        );
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

    private GroupInvite createUniqueInvite(GroupRoom room, Member member, LocalDateTime expiresAt) {
        for (int attempt = 0; attempt < MAX_INVITE_CODE_GENERATION_ATTEMPTS; attempt++) {
            String inviteCode = groupInviteCodeGenerator.generate();

            if (!groupInviteRepository.existsByInviteCode(inviteCode)) {
                return groupInviteRepository.save(new GroupInvite(room, member, inviteCode, expiresAt));
            }
        }

        throw new BusinessException(GroupErrorCode.INVITE_CODE_GENERATION_FAILED);
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
