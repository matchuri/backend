package matchuri.backend.domain.group.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.command.CreateGroupCommand;
import matchuri.backend.domain.group.command.DeleteGroupCommand;
import matchuri.backend.domain.group.command.GetMyGroupsCommand;
import matchuri.backend.domain.group.command.LeaveGroupCommand;
import matchuri.backend.domain.group.command.UpdateGroupCommand;
import matchuri.backend.domain.group.entity.GroupInviteStatus;
import matchuri.backend.domain.group.entity.GroupLocation;
import matchuri.backend.domain.group.entity.GroupMemberStatus;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.exception.GroupErrorCode;
import matchuri.backend.domain.group.repository.GroupInviteRepository;
import matchuri.backend.domain.group.repository.GroupRecommendationRepository;
import matchuri.backend.domain.group.repository.GroupRoomMemberCountProjection;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.group.result.CreateGroupResult;
import matchuri.backend.domain.group.result.DeleteGroupResult;
import matchuri.backend.domain.group.result.GroupDetailResult;
import matchuri.backend.domain.group.result.GroupMemberSummaryResult;
import matchuri.backend.domain.group.result.GroupRecommendationResult;
import matchuri.backend.domain.group.result.GroupSummaryResult;
import matchuri.backend.domain.group.result.LeaveGroupResult;
import matchuri.backend.domain.group.result.UpdateGroupResult;
import matchuri.backend.domain.group.service.GroupManagementService;
import matchuri.backend.domain.group.support.GroupInviteCodeGenerator;
import matchuri.backend.domain.group.support.GroupInviteLinkManager;
import matchuri.backend.domain.group.support.location.GroupLocationManager;
import matchuri.backend.domain.group.support.recommendation.GroupRecommendationExpirationManager;
import matchuri.backend.domain.group.support.recommendation.GroupRecommendationResultAssembler;
import matchuri.backend.domain.group.support.room.GroupRoomReader;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.support.member.MemberReader;
import matchuri.backend.domain.realtime.event.GroupDeletedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupMemberLeftRealtimeEvent;
import matchuri.backend.global.exception.BusinessException;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupManagementServiceImpl implements GroupManagementService {

    private static final int MAX_INVITE_CODE_GENERATION_ATTEMPTS = 5;

    private final MemberReader memberReader;
    private final GroupRoomRepository groupRoomRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupInviteRepository groupInviteRepository;
    private final GroupRecommendationRepository groupRecommendationRepository;
    private final GroupInviteCodeGenerator groupInviteCodeGenerator;
    private final GroupInviteLinkManager groupInviteLinkManager;
    private final GroupRoomReader groupRoomReader;
    private final GroupLocationManager groupLocationManager;
    private final GroupRecommendationExpirationManager groupRecommendationExpirationManager;
    private final GroupRecommendationResultAssembler groupRecommendationResultAssembler;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CreateGroupResult createGroup(Long memberId, CreateGroupCommand command) {
        Member hostMember = memberReader.getActiveMember(memberId);
        String inviteCode = createUniqueInviteCode();

        GroupRoom groupRoom = GroupRoom.createOwnedBy(
                command.name(),
                inviteCode,
                hostMember);

        GroupRoom savedGroupRoom = groupRoomRepository.save(groupRoom);
        groupLocationManager.updateLatestGroupLocation(
                savedGroupRoom,
                command.latitude(),
                command.longitude(),
                command.radiusMeters(),
                command.address()
        );

        return new CreateGroupResult(
                savedGroupRoom.getId(),
                savedGroupRoom.getInviteCode(),
                savedGroupRoom.getStatus()
        );
    }

    @Override
    public LeaveGroupResult leaveGroup(Long memberId, LeaveGroupCommand command) {
        Member member = memberReader.getActiveMember(memberId);
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

        eventPublisher.publishEvent(new GroupMemberLeftRealtimeEvent(
                room.getId(),
                member.getId(),
                member.getNickname(),
                membership.getLeftAt()
        ));

        return new LeaveGroupResult(room.getId(), membership.getStatus(), membership.getLeftAt());
    }

    @Override
    @Transactional
    public DeleteGroupResult deleteGroup(Long memberId, DeleteGroupCommand command) {
        Member member = memberReader.getActiveMember(memberId);
        GroupRoom room = groupRoomRepository.findByIdAndStatusNotForUpdate(command.groupId(), GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, command.groupId()));
        GroupRoomMember membership = groupRoomMemberRepository
                .findActiveMembershipInNotDeletedRoom(room.getId(), member.getId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.ACCESS_DENIED, room.getId()));

        if (!membership.isOwner()) {
            throw new BusinessException(GroupErrorCode.DELETE_FORBIDDEN, room.getId());
        }

        LocalDateTime deletedAt = LocalDateTime.now();
        List<Long> targetMemberIds = groupRoomMemberRepository.findActiveMembersByRoomId(room.getId()).stream()
                .map(GroupRoomMember::getMember)
                .map(Member::getId)
                .toList();

        room.delete(deletedAt);
        revokeActiveInvites(room);
        groupInviteLinkManager.expireAllActive(room, deletedAt);
        leaveActiveMembers(room, deletedAt);

        eventPublisher.publishEvent(new GroupDeletedRealtimeEvent(
                room.getId(),
                member.getId(),
                targetMemberIds,
                deletedAt
        ));

        return new DeleteGroupResult(room.getId(), room.getStatus(), deletedAt);
    }

    @Override
    public UpdateGroupResult updateGroup(Long memberId, UpdateGroupCommand command) {
        if (command.hasNoFields()) {
            throw new BusinessException(GroupErrorCode.UPDATE_EMPTY_REQUEST);
        }

        Member member = memberReader.getActiveMember(memberId);
        GroupRoom room = groupRoomReader.getActiveGroupRoom(command.groupId());

        GroupRoomMember membership = room.getGroupRoomMemberById(member.getId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.ACCESS_DENIED, room.getId()));

        if (!membership.isOwner()) {
            throw new BusinessException(GroupErrorCode.UPDATE_FORBIDDEN, room.getId());
        }

        if (command.name() != null) {
            room.updateName(command.name());
        }

        GroupLocation location = groupLocationManager.updateLatestGroupLocation(
                room,
                command.latitude(),
                command.longitude(),
                command.radiusMeters(),
                command.address()
        );

        return new UpdateGroupResult(
                room.getId(),
                room.getName(),
                location == null ? null : location.getLatitude(),
                location == null ? null : location.getLongitude(),
                location == null ? null : location.getRadiusMeters(),
                location == null ? null : location.getAddress(),
                room.getStatus(),
                room.getUpdatedAt()
        );
    }

    @Override
    public Page<@NonNull GroupSummaryResult> getMyGroups(Long memberId, GetMyGroupsCommand command) {
        Member member = memberReader.getActiveMember(memberId);
        Page<@NonNull GroupRoomMember> memberships = groupRoomMemberRepository.findMyActiveMemberships(
                member.getId(),
                command.status(),
                PageRequest.of(command.page(), command.size())
        );
        Map<Long, Long> activeMemberCounts = countActiveMembers(memberships);
        List<Long> roomIds = memberships.getContent().stream()
                .map(membership -> membership.getRoom().getId())
                .toList();
        groupRecommendationExpirationManager.expireActiveGroupRecommendations(
                roomIds,
                LocalDateTime.now()
        );
        Map<Long, GroupRecommendationStatus> latestRecommendationStatuses =
                groupRecommendationExpirationManager.latestRecommendationStatuses(roomIds);

        return memberships.map(membership -> toSummaryResult(
                membership,
                activeMemberCounts,
                latestRecommendationStatuses
        ));
    }

    @Override
    public GroupDetailResult getGroup(Long memberId, Long groupId) {
        Member member = memberReader.getActiveMember(memberId);
        GroupRoom room = groupRoomRepository.findByIdAndStatusNot(groupId, GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, groupId));

        List<GroupRoomMember> activeMemberships = groupRoomMemberRepository.findActiveMembersByRoomId(groupId);
        if (activeMemberships.stream()
                .map(GroupRoomMember::getMember)
                .map(Member::getId)
                .noneMatch(member.getId()::equals)) {
            throw new BusinessException(GroupErrorCode.ACCESS_DENIED, groupId);
        }

        List<GroupMemberSummaryResult> members = activeMemberships.stream()
                .map(membership -> toMemberSummaryResult(membership, member.getId()))
                .toList();
        groupRecommendationExpirationManager.expireActiveGroupRecommendations(groupId, LocalDateTime.now());
        GroupRecommendationResult recentlyRecommendation = groupRecommendationRepository
                .findFirstByRoomIdOrderByCreatedAtDescIdDesc(groupId)
                .map(recommendation -> groupRecommendationResultAssembler.toGroupRecommendationResult(
                        recommendation,
                        member.getId(),
                        activeMemberships
                ))
                .orElse(null);
        GroupLocation location = groupLocationManager.latestGroupLocation(room.getId());

        return new GroupDetailResult(
                room.getId(),
                room.getName(),
                room.getInviteCode(),
                location == null ? null : location.getLatitude(),
                location == null ? null : location.getLongitude(),
                location == null ? null : location.getRadiusMeters(),
                location == null ? null : location.getAddress(),
                room.getStatus(),
                members,
                recentlyRecommendation
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
            Map<Long, Long> activeMemberCounts,
            Map<Long, GroupRecommendationStatus> latestRecommendationStatuses
    ) {
        GroupRoom room = membership.getRoom();

        return new GroupSummaryResult(
                room.getId(),
                room.getName(),
                room.getStatus(),
                activeMemberCounts.getOrDefault(room.getId(), 0L).intValue(),
                latestRecommendationStatuses.get(room.getId()),
                room.getCreatedAt()
        );
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

    private void revokeActiveInvites(GroupRoom room) {
        groupInviteRepository.findAllByRoomIdAndStatus(room.getId(), GroupInviteStatus.PENDING)
                .forEach(groupInvite -> groupInvite.revoke());
    }

    private void leaveActiveMembers(GroupRoom room, LocalDateTime leftAt) {
        groupRoomMemberRepository.findActiveMembersByRoomId(room.getId())
                .forEach(membership -> membership.leave(leftAt));
    }

    private GroupMemberSummaryResult toMemberSummaryResult(GroupRoomMember membership, Long currentMemberId) {
        Member member = membership.getMember();

        return new GroupMemberSummaryResult(
                member.getId(),
                member.getNickname(),
                membership.getRole(),
                membership.getStatus(),
                membership.getJoinedAt(),
                member.getId().equals(currentMemberId)
        );
    }
}



