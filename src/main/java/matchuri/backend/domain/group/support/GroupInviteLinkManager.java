package matchuri.backend.domain.group.support;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.entity.GroupInviteLink;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.exception.GroupErrorCode;
import matchuri.backend.domain.group.repository.GroupInviteLinkRepository;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupInviteLinkManager {

    private static final int MAX_TOKEN_GENERATION_ATTEMPTS = 5;
    private static final int EXPIRATION_DAYS = 1;

    private final GroupRoomRepository groupRoomRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupInviteLinkRepository groupInviteLinkRepository;
    private final GroupInviteLinkTokenGenerator tokenGenerator;

    public GroupInviteLink create(Long groupId, Long memberId, LocalDateTime issuedAt) {
        GroupRoom room = getActiveGroupRoomForUpdate(groupId, memberId);
        if (findCurrent(room.getId(), issuedAt).isPresent()) {
            throw new BusinessException(GroupErrorCode.INVITE_LINK_ALREADY_EXISTS, room.getId());
        }
        return createUnique(room, issuedAt);
    }

    public GroupInviteLink reissue(Long groupId, Long memberId, LocalDateTime issuedAt) {
        GroupRoom room = getActiveGroupRoomForUpdate(groupId, memberId);
        GroupInviteLink current = findCurrent(room.getId(), issuedAt)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.INVITE_LINK_NOT_FOUND));
        current.expire(issuedAt);
        return createUnique(room, issuedAt);
    }

    public Optional<GroupInviteLink> getCurrent(Long groupId, Long memberId, LocalDateTime now) {
        GroupRoom room = getActiveGroupRoom(groupId);
        validateOwner(room.getId(), memberId);
        return findCurrent(room.getId(), now);
    }

    public GroupInviteLink getJoinable(String token, LocalDateTime now) {
        GroupInviteLink inviteLink = groupInviteLinkRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.INVITE_LINK_NOT_FOUND));
        if (inviteLink.isExpired(now)) {
            throw new BusinessException(GroupErrorCode.INVITE_LINK_EXPIRED);
        }
        if (!inviteLink.getRoom().isActive()) {
            throw new BusinessException(GroupErrorCode.NOT_ACTIVE, inviteLink.getRoom().getId());
        }
        return inviteLink;
    }

    public void expireAllActive(GroupRoom room, LocalDateTime expiredAt) {
        groupInviteLinkRepository.findAllByRoomIdAndExpiresAtAfter(room.getId(), expiredAt)
                .forEach(inviteLink -> inviteLink.expire(expiredAt));
    }

    private GroupRoom getActiveGroupRoomForUpdate(Long groupId, Long memberId) {
        GroupRoom room = groupRoomRepository.findByIdAndStatusNotForUpdate(groupId, GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, groupId));
        validateActive(room);
        validateOwner(room.getId(), memberId);
        return room;
    }

    private GroupRoom getActiveGroupRoom(Long groupId) {
        GroupRoom room = groupRoomRepository.findByIdAndStatusNot(groupId, GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, groupId));
        validateActive(room);
        return room;
    }

    private void validateActive(GroupRoom room) {
        if (!room.isActive()) {
            throw new BusinessException(GroupErrorCode.NOT_ACTIVE, room.getId());
        }
    }

    private void validateOwner(Long groupId, Long memberId) {
        GroupRoomMember membership = groupRoomMemberRepository
                .findActiveMembershipInNotDeletedRoom(groupId, memberId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.INVITE_FORBIDDEN, groupId));
        if (!membership.isOwner()) {
            throw new BusinessException(GroupErrorCode.INVITE_FORBIDDEN, groupId);
        }
    }

    private Optional<GroupInviteLink> findCurrent(Long groupId, LocalDateTime now) {
        return groupInviteLinkRepository
                .findFirstByRoomIdAndExpiresAtAfterOrderByCreatedAtDescIdDesc(groupId, now);
    }

    private GroupInviteLink createUnique(GroupRoom room, LocalDateTime issuedAt) {
        for (int attempt = 0; attempt < MAX_TOKEN_GENERATION_ATTEMPTS; attempt++) {
            String token = tokenGenerator.generate();
            if (!groupInviteLinkRepository.existsByToken(token)) {
                return groupInviteLinkRepository.save(
                        new GroupInviteLink(room, token, issuedAt.plusDays(EXPIRATION_DAYS))
                );
            }
        }
        throw new BusinessException(GroupErrorCode.INVITE_LINK_TOKEN_GENERATION_FAILED);
    }
}
