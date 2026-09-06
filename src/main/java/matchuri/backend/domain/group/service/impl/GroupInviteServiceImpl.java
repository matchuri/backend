package matchuri.backend.domain.group.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.command.CreateNicknameGroupInviteCommand;
import matchuri.backend.domain.group.command.GetMyGroupInvitesCommand;
import matchuri.backend.domain.group.command.JoinGroupCommand;
import matchuri.backend.domain.group.command.RespondGroupInviteCommand;
import matchuri.backend.domain.group.entity.GroupInvite;
import matchuri.backend.domain.group.entity.GroupInviteLink;
import matchuri.backend.domain.group.entity.GroupInviteResponseType;
import matchuri.backend.domain.group.entity.GroupInviteStatus;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupMemberStatus;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.exception.GroupErrorCode;
import matchuri.backend.domain.group.repository.GroupInviteRepository;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.group.result.CreateNicknameGroupInviteResult;
import matchuri.backend.domain.group.result.GroupInviteLinkResult;
import matchuri.backend.domain.group.result.GroupInviteSummaryResult;
import matchuri.backend.domain.group.result.GroupInviteV2SummaryResult;
import matchuri.backend.domain.group.result.JoinGroupResult;
import matchuri.backend.domain.group.result.RespondGroupInviteResult;
import matchuri.backend.domain.group.service.GroupInviteService;
import matchuri.backend.domain.group.support.GroupInviteLinkManager;
import matchuri.backend.domain.group.support.room.GroupRoomReader;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.support.member.MemberReader;
import matchuri.backend.domain.member.support.profile.MemberProfileImageUrlResolver;
import matchuri.backend.domain.realtime.event.GroupInviteCreatedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupMemberJoinedRealtimeEvent;
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
public class GroupInviteServiceImpl implements GroupInviteService {

    private static final int NICKNAME_INVITE_EXPIRATION_HOURS = 24;

    private final MemberReader memberReader;
    private final MemberRepository memberRepository;
    private final GroupRoomRepository groupRoomRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupInviteRepository groupInviteRepository;
    private final GroupInviteLinkManager groupInviteLinkManager;
    private final GroupRoomReader groupRoomReader;
    private final MemberProfileImageUrlResolver memberProfileImageUrlResolver;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CreateNicknameGroupInviteResult createNicknameInvite(Long memberId, CreateNicknameGroupInviteCommand command) {
        Member requestMember = memberReader.getActiveMember(memberId);
        GroupRoom room = groupRoomReader.getActiveGroupRoom(command.groupId());
        validateGroupRoomMember(room, requestMember);

        Member targetMember = memberRepository.findByActiveMemberByNickname(command.nickname())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.INVITE_TARGET_NOT_FOUND, command.nickname()));
        validateTargetMember(requestMember, targetMember, room);

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(NICKNAME_INVITE_EXPIRATION_HOURS);
        GroupInvite invite = groupInviteRepository.save(new GroupInvite(room, requestMember, targetMember, expiresAt));

        eventPublisher.publishEvent(new GroupInviteCreatedRealtimeEvent(
                invite.getId(),
                room.getId(),
                room.getName(),
                requestMember.getId(),
                requestMember.getNickname(),
                targetMember.getId(),
                invite.getExpiresAt()
        ));

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

    private void validateGroupRoomMember(GroupRoom room, Member member) {
        GroupRoomMember requestMembership = groupRoomMemberRepository
                .findActiveMembershipInNotDeletedRoom(room.getId(), member.getId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.INVITE_FORBIDDEN, room.getId()));

        if (!requestMembership.isOwner()) {
            throw new BusinessException(GroupErrorCode.INVITE_FORBIDDEN, room.getId());
        }
    }

    private void validateTargetMember(Member requestMember, Member targetMember, GroupRoom room) {
        if (requestMember.getId().equals(targetMember.getId())) {
            throw new BusinessException(GroupErrorCode.INVITE_SELF_NOT_ALLOWED, targetMember.getId());
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
    }

    @Override
    public GroupInviteLinkResult createInviteLink(Long memberId, Long groupId) {
        Member member = memberReader.getActiveMember(memberId);
        return GroupInviteLinkResult.from(
                groupInviteLinkManager.create(groupId, member.getId(), LocalDateTime.now())
        );
    }

    @Override
    public GroupInviteLinkResult reissueInviteLink(Long memberId, Long groupId) {
        Member member = memberReader.getActiveMember(memberId);
        return GroupInviteLinkResult.from(
                groupInviteLinkManager.reissue(groupId, member.getId(), LocalDateTime.now())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public GroupInviteLinkResult getCurrentInviteLink(Long memberId, Long groupId) {
        Member member = memberReader.getActiveMember(memberId);
        return GroupInviteLinkResult.from(
                groupInviteLinkManager.getCurrent(groupId, member.getId(), LocalDateTime.now())
        );
    }

    @Override
    public JoinGroupResult joinGroupByInviteLink(Long memberId, String token) {
        Member member = memberReader.getActiveMember(memberId);
        GroupInviteLink inviteLink = groupInviteLinkManager.getJoinable(token, LocalDateTime.now());
        return joinGroup(inviteLink.getRoom(), member);
    }

    @Override
    public JoinGroupResult joinGroup(Long memberId, JoinGroupCommand command) {
        Member member = memberReader.getActiveMember(memberId);
        GroupRoom room = groupRoomRepository.findByInviteCode(command.inviteCode())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.INVITE_NOT_FOUND, command.inviteCode()));

        if (!room.isActive()) {
            throw new BusinessException(GroupErrorCode.NOT_ACTIVE, room.getId());
        }

        return joinGroup(room, member);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull GroupInviteSummaryResult> getMyInvites(Long memberId, GetMyGroupInvitesCommand command) {
        Member member = memberReader.getActiveMember(memberId);
        GroupInviteStatus status = command.status() == null ? GroupInviteStatus.PENDING : command.status();

        return groupInviteRepository.findMyInvites(
                member.getId(),
                status,
                LocalDateTime.now(),
                PageRequest.of(command.page(), command.size())).map(this::toInviteSummaryResult);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull GroupInviteV2SummaryResult> getMyInvitesV2(
            Long memberId,
            GetMyGroupInvitesCommand command
    ) {
        Member member = memberReader.getActiveMember(memberId);
        GroupInviteStatus status = command.status() == null ? GroupInviteStatus.PENDING : command.status();
        Page<GroupInvite> invites = groupInviteRepository.findMyInvites(
                member.getId(),
                status,
                LocalDateTime.now(),
                PageRequest.of(command.page(), command.size())
        );
        List<Long> requestMemberIds = invites.stream()
                .map(GroupInvite::getRequestMember)
                .map(Member::getId)
                .distinct()
                .toList();
        Map<Long, String> profileImageUrlsByMemberId = memberProfileImageUrlResolver.resolveAll(requestMemberIds);

        return invites.map(invite -> GroupInviteV2SummaryResult.from(
                invite,
                invite.getRoom().getName(),
                profileImageUrlsByMemberId.get(invite.getRequestMember().getId())
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsMyPendingInvite(Long memberId) {
        Member member = memberReader.getActiveMember(memberId);

        return groupInviteRepository.existsByTargetMemberIdAndStatusAndExpiresAtAfter(
                member.getId(),
                GroupInviteStatus.PENDING,
                LocalDateTime.now()
        );
    }

    @Override
    public RespondGroupInviteResult respondGroupInvite(Long memberId, RespondGroupInviteCommand command) {
        Member member = memberReader.getActiveMember(memberId);
        GroupInvite invite = groupInviteRepository.findById(command.inviteId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.INVITE_REQUEST_NOT_FOUND, command.inviteId()));

        if (!invite.getTargetMember().getId().equals(member.getId())) {
            throw new BusinessException(GroupErrorCode.INVITE_RESPONSE_FORBIDDEN, invite.getId());
        }

        LocalDateTime now = LocalDateTime.now();
        validateRespondableInvite(invite, now);

        GroupRoom room = groupRoomReader.getActiveGroupRoom(invite.getRoom().getId());
        GroupMemberStatus memberStatus = null;

        if (command.responseType() == GroupInviteResponseType.ACCEPT) {

            GroupRoomMember membership = joinOrRejoinMember(room, member);
            memberStatus = membership.getStatus();
            invite.accept(now);
            eventPublisher.publishEvent(new GroupMemberJoinedRealtimeEvent(
                    room.getId(),
                    member.getId(),
                    member.getNickname(),
                    membership.getJoinedAt()
            ));
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

    private JoinGroupResult joinGroup(GroupRoom room, Member member) {
        GroupRoomMember membership = joinOrRejoinMember(room, member);

        eventPublisher.publishEvent(new GroupMemberJoinedRealtimeEvent(
                room.getId(),
                member.getId(),
                member.getNickname(),
                membership.getJoinedAt()
        ));

        return new JoinGroupResult(room.getId(), membership.getStatus());
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
}
