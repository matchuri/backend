package matchuri.backend.domain.group.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.command.CreateGroupCommand;
import matchuri.backend.domain.group.command.CreateNicknameGroupInviteCommand;
import matchuri.backend.domain.group.command.DeleteGroupCommand;
import matchuri.backend.domain.group.command.GetMyGroupInvitesCommand;
import matchuri.backend.domain.group.command.GetMyGroupsCommand;
import matchuri.backend.domain.group.command.JoinGroupCommand;
import matchuri.backend.domain.group.command.LeaveGroupCommand;
import matchuri.backend.domain.group.command.RespondGroupInviteCommand;
import matchuri.backend.domain.group.command.UpdateGroupCommand;
import matchuri.backend.domain.group.entity.GroupInvite;
import matchuri.backend.domain.group.entity.GroupInviteResponseType;
import matchuri.backend.domain.group.entity.GroupInviteStatus;
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
import matchuri.backend.domain.group.result.CreateGroupResult;
import matchuri.backend.domain.group.result.CreateNicknameGroupInviteResult;
import matchuri.backend.domain.group.result.DeleteGroupResult;
import matchuri.backend.domain.group.result.GroupDetailResult;
import matchuri.backend.domain.group.result.GroupInviteSummaryResult;
import matchuri.backend.domain.group.result.GroupMemberSummaryResult;
import matchuri.backend.domain.group.result.GroupSummaryResult;
import matchuri.backend.domain.group.result.JoinGroupResult;
import matchuri.backend.domain.group.result.LeaveGroupResult;
import matchuri.backend.domain.group.result.RespondGroupInviteResult;
import matchuri.backend.domain.group.result.UpdateGroupResult;
import matchuri.backend.domain.group.support.GroupInviteCodeGenerator;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
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

    private static final int MAX_INVITE_CODE_GENERATION_ATTEMPTS = 5;
    private static final int NICKNAME_INVITE_EXPIRATION_HOURS = 24;

    private final ActiveMemberReader activeMemberReader;
    private final MemberRepository memberRepository;
    private final GroupRoomRepository groupRoomRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupInviteRepository groupInviteRepository;
    private final GroupInviteCodeGenerator groupInviteCodeGenerator;

    @Override
    public CreateGroupResult createGroup(CreateGroupCommand command) {
        Member hostMember = activeMemberReader.getCurrentAuthenticatedActiveMember();
        String inviteCode = createUniqueInviteCode();

        GroupRoom groupRoom = GroupRoom.createOwnedBy(
                command.name(),
                inviteCode,
                hostMember,
                command.latitude(),
                command.longitude());

        GroupRoom savedGroupRoom = groupRoomRepository.save(groupRoom);

        return new CreateGroupResult(
                savedGroupRoom.getId(),
                savedGroupRoom.getInviteCode(),
                savedGroupRoom.getStatus()
        );
    }

    @Override
    public CreateNicknameGroupInviteResult createNicknameInvite(CreateNicknameGroupInviteCommand command) {
        Member requestMember = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = groupRoomRepository.findByIdAndStatusNot(command.groupId(), GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, command.groupId()));

        if (!room.isActive()) {
            throw new BusinessException(GroupErrorCode.NOT_ACTIVE, command.groupId());
        }

        GroupRoomMember requestMembership = groupRoomMemberRepository
                .findActiveMembershipInNotDeletedRoom(room.getId(), requestMember.getId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.INVITE_FORBIDDEN, room.getId()));

        if (!requestMembership.isOwner()) {
            throw new BusinessException(GroupErrorCode.INVITE_FORBIDDEN, room.getId());
        }

        Member targetMember = memberRepository.findByNicknameAndStatus(command.nickname(), MemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.INVITE_TARGET_NOT_FOUND, command.nickname()));

        if (requestMember.getId().equals(targetMember.getId())) {
            throw new BusinessException(GroupErrorCode.INVITE_SELF_NOT_ALLOWED, requestMember.getId());
        }

        if (groupRoomMemberRepository.existsActiveMembershipInNotDeletedRoom(room.getId(), targetMember.getId())) {
            throw new BusinessException(
                    GroupErrorCode.INVITE_TARGET_ALREADY_MEMBER,
                    room.getId(),
                    targetMember.getId()
            );
        }

        if (groupInviteRepository.existsByRoomIdAndTargetMemberIdAndStatus(
                room.getId(),
                targetMember.getId(),
                GroupInviteStatus.PENDING
        )) {
            throw new BusinessException(GroupErrorCode.INVITE_ALREADY_PENDING, room.getId(), targetMember.getId());
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(NICKNAME_INVITE_EXPIRATION_HOURS);
        GroupInvite invite = groupInviteRepository.save(new GroupInvite(room, requestMember, targetMember, expiresAt));

        return new CreateNicknameGroupInviteResult(
                invite.getId(),
                room.getId(),
                room.getName(),
                targetMember.getId(),
                targetMember.getNickname(),
                invite.getExpiresAt(),
                invite.getStatus()
        );
    }

    @Override
    public JoinGroupResult joinGroup(JoinGroupCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = groupRoomRepository.findByInviteCode(command.inviteCode())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.INVITE_NOT_FOUND, command.inviteCode()));

        if (!room.isActive()) {
            throw new BusinessException(GroupErrorCode.NOT_ACTIVE, room.getId());
        }

        GroupRoomMember membership = joinOrRejoinMember(room, member);

        return new JoinGroupResult(room.getId(), membership.getStatus());
    }

    @Override
    public LeaveGroupResult leaveGroup(LeaveGroupCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        Long memberId = member.getId();
        GroupRoom room = groupRoomRepository.findByIdAndStatusNot(command.groupId(), GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, command.groupId()));
        GroupRoomMember membership = room.getGroupRoomMemberById(memberId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.MEMBER_NOT_FOUND, room.getId(), memberId));

        if (membership.isLeft()) {
            throw new BusinessException(GroupErrorCode.MEMBER_ALREADY_LEFT, room.getId(), memberId);
        }

        if (!membership.isActive()) {
            throw new BusinessException(GroupErrorCode.MEMBER_NOT_FOUND, room.getId(), memberId);
        }

        if (membership.isOwner()) {
            throw new BusinessException(GroupErrorCode.OWNER_LEAVE_NOT_ALLOWED, room.getId());
        }

        LocalDateTime leftAt = LocalDateTime.now();
        membership.leave(leftAt);

        return new LeaveGroupResult(room.getId(), membership.getStatus(), membership.getLeftAt());
    }

    @Override
    @Transactional
    public DeleteGroupResult deleteGroup(DeleteGroupCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = groupRoomRepository.findByIdAndStatusNot(command.groupId(), GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, command.groupId()));
        GroupRoomMember membership = groupRoomMemberRepository
                .findActiveMembershipInNotDeletedRoom(room.getId(), member.getId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.ACCESS_DENIED, room.getId()));

        if (!membership.isOwner()) {
            throw new BusinessException(GroupErrorCode.DELETE_FORBIDDEN, room.getId());
        }

        LocalDateTime deletedAt = LocalDateTime.now();
        room.delete();
        revokeActiveInvites(room);
        leaveActiveMembers(room, deletedAt);

        return new DeleteGroupResult(room.getId(), room.getStatus(), deletedAt);
    }

    @Override
    public UpdateGroupResult updateGroup(UpdateGroupCommand command) {
        if (command.hasNoFields()) {
            throw new BusinessException(GroupErrorCode.UPDATE_EMPTY_REQUEST);
        }

        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = groupRoomRepository.findByIdAndStatusNot(command.groupId(), GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, command.groupId()));

        if (!room.isActive()) {
            throw new BusinessException(GroupErrorCode.NOT_ACTIVE, room.getId());
        }

        GroupRoomMember membership = room.getGroupRoomMemberById(member.getId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.ACCESS_DENIED, room.getId()));

        if (!membership.isOwner()) {
            throw new BusinessException(GroupErrorCode.UPDATE_FORBIDDEN, room.getId());
        }

        if (command.name() != null) {
            room.updateName(command.name());
        }

        if (command.latitude() != null) {
            room.updateLatitude(command.latitude());
        }

        if (command.longitude() != null) {
            room.updateLongitude(command.longitude());
        }

        return new UpdateGroupResult(
                room.getId(),
                room.getName(),
                room.getLatitude(),
                room.getLongitude(),
                room.getStatus(),
                room.getUpdatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull GroupSummaryResult> getMyGroups(GetMyGroupsCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        Page<@NonNull GroupRoomMember> memberships = groupRoomMemberRepository.findMyActiveMemberships(
                member.getId(),
                command.status(),
                PageRequest.of(command.page(), command.size())
        );
        Map<Long, Long> activeMemberCounts = countActiveMembers(memberships);

        return memberships.map(membership -> toSummaryResult(membership, activeMemberCounts));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull GroupInviteSummaryResult> getMyInvites(GetMyGroupInvitesCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupInviteStatus status = command.status() == null ? GroupInviteStatus.PENDING : command.status();

        return groupInviteRepository.findMyInvites(
                member.getId(),
                status,
                PageRequest.of(command.page(), command.size())).map(this::toInviteSummaryResult);
    }

    @Override
    public RespondGroupInviteResult respondGroupInvite(RespondGroupInviteCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupInvite invite = groupInviteRepository.findById(command.inviteId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.INVITE_REQUEST_NOT_FOUND, command.inviteId()));

        if (!invite.getTargetMember().getId().equals(member.getId())) {
            throw new BusinessException(GroupErrorCode.INVITE_RESPONSE_FORBIDDEN, invite.getId());
        }

        LocalDateTime now = LocalDateTime.now();
        validateRespondableInvite(invite, now);

        GroupRoom room = invite.getRoom();
        GroupMemberStatus memberStatus = null;

        if (!room.isActive()) {
            throw new BusinessException(GroupErrorCode.NOT_ACTIVE, room.getId());
        }

        if (command.responseType() == GroupInviteResponseType.ACCEPT) {

            GroupRoomMember membership = joinOrRejoinMember(room, member);
            memberStatus = membership.getStatus();
            invite.accept(now);
        } else {
            invite.decline(now);
        }

        return new RespondGroupInviteResult(
                invite.getId(),
                room.getId(),
                invite.getStatus(),
                memberStatus,
                invite.getRespondedAt()
        );
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
                room.getInviteCode(),
                room.getLatitude(),
                room.getLongitude(),
                room.getStatus(),
                members
        );
    }

    private Map<Long, Long> countActiveMembers(Page<@NonNull GroupRoomMember> memberships) {
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

    private GroupInviteSummaryResult toInviteSummaryResult(GroupInvite invite) {
        GroupRoom room = invite.getRoom();
        Member requestMember = invite.getRequestMember();

        return new GroupInviteSummaryResult(
                invite.getId(),
                room.getId(),
                room.getName(),
                requestMember.getId(),
                requestMember.getNickname(),
                invite.getStatus(),
                invite.getExpiresAt(),
                invite.getCreatedAt()
        );
    }

    private void validateRespondableInvite(GroupInvite invite, LocalDateTime now) {
        if (!invite.isPending()) {
            throw new BusinessException(GroupErrorCode.INVITE_NOT_PENDING, invite.getId(), invite.getStatus());
        }

        if (invite.isExpired(now)) {
            throw new BusinessException(GroupErrorCode.INVITE_EXPIRED, invite.getId());
        }
    }

    private String createUniqueInviteCode() {
        for (int attempt = 0; attempt < MAX_INVITE_CODE_GENERATION_ATTEMPTS; attempt++) {
            String inviteCode = groupInviteCodeGenerator.generate();

            if (!groupRoomRepository.existsByInviteCode(inviteCode)) {
                return inviteCode;
            }
        }

        throw new BusinessException(GroupErrorCode.INVITE_CODE_GENERATION_FAILED);
    }

    private GroupRoomMember joinOrRejoinMember(GroupRoom room, Member member) {
        return groupRoomMemberRepository.findByRoomIdAndMemberId(room.getId(), member.getId())
                .map(membership -> rejoinExistingMembership(room, member, membership))
                .orElseGet(() -> groupRoomMemberRepository.save(
                        new GroupRoomMember(room, member, GroupMemberRole.MEMBER, LocalDateTime.now())
                ));
    }

    private GroupRoomMember rejoinExistingMembership(
            GroupRoom room,
            Member member,
            GroupRoomMember membership
    ) {
        if (membership.getStatus() == GroupMemberStatus.ACTIVE) {
            throw new BusinessException(GroupErrorCode.ALREADY_JOINED, room.getId(), member.getId());
        }

        if (membership.getStatus() == GroupMemberStatus.LEFT) {
            membership.rejoin(LocalDateTime.now());
            return membership;
        }

        throw new BusinessException(GroupErrorCode.ACCESS_DENIED, room.getId());
    }

    private void revokeActiveInvites(GroupRoom room) {
        groupInviteRepository.findAllByRoomIdAndStatus(room.getId(), GroupInviteStatus.PENDING)
                .forEach(groupInvite -> groupInvite.revoke());
    }

    private void leaveActiveMembers(GroupRoom room, LocalDateTime leftAt) {
        groupRoomMemberRepository.findActiveMembersByRoomId(room.getId())
                .forEach(membership -> membership.leave(leftAt));
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
