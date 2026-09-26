package matchuri.backend.domain.group.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupInviteLink;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.exception.GroupErrorCode;
import matchuri.backend.domain.group.repository.GroupInviteLinkRepository;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupInviteLinkManagerTest {

    private static final long MEMBER_ID = 1L;
    private static final long GROUP_ID = 10L;
    private static final String TOKEN = "550e8400-e29b-41d4-a716-446655440000";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 15, 12, 0);

    @Mock private GroupRoomRepository groupRoomRepository;
    @Mock private GroupRoomMemberRepository groupRoomMemberRepository;
    @Mock private GroupInviteLinkRepository groupInviteLinkRepository;
    @Mock private GroupInviteLinkTokenGenerator tokenGenerator;

    @InjectMocks
    private GroupInviteLinkManager manager;

    @Test
    @DisplayName("초대 링크 신규 발급은 생성 시각부터 1일 유효한 토큰을 저장한다")
    void createStoresOneDayToken() {
        stubOwnerUpdateAccess();
        when(groupInviteLinkRepository.findFirstByRoomIdAndExpiresAtAfterOrderByCreatedAtDescIdDesc(GROUP_ID, NOW))
                .thenReturn(Optional.empty());
        when(tokenGenerator.generate()).thenReturn(TOKEN);
        when(groupInviteLinkRepository.existsByToken(TOKEN)).thenReturn(false);
        when(groupInviteLinkRepository.save(any(GroupInviteLink.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GroupInviteLink result = manager.create(GROUP_ID, MEMBER_ID, NOW);

        assertThat(result.getRoom().getId()).isEqualTo(GROUP_ID);
        assertThat(result.getToken()).isEqualTo(TOKEN);
        assertThat(result.getExpiresAt()).isEqualTo(NOW.plusDays(1));
        verify(groupInviteLinkRepository).save(any(GroupInviteLink.class));
    }

    @Test
    @DisplayName("활성 초대 링크가 있으면 신규 발급을 거절한다")
    void createRejectsActiveLink() {
        GroupRoom room = stubOwnerUpdateAccess();
        GroupInviteLink activeLink = new GroupInviteLink(room, TOKEN, NOW.plusHours(1));
        when(groupInviteLinkRepository.findFirstByRoomIdAndExpiresAtAfterOrderByCreatedAtDescIdDesc(GROUP_ID, NOW))
                .thenReturn(Optional.of(activeLink));

        assertThatThrownBy(() -> manager.create(GROUP_ID, MEMBER_ID, NOW))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.INVITE_LINK_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("현재 링크가 없으면 재발급을 거절한다")
    void reissueRejectsMissingCurrentLink() {
        stubOwnerUpdateAccess();
        when(groupInviteLinkRepository.findFirstByRoomIdAndExpiresAtAfterOrderByCreatedAtDescIdDesc(GROUP_ID, NOW))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> manager.reissue(GROUP_ID, MEMBER_ID, NOW))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.INVITE_LINK_NOT_FOUND);
    }

    @Test
    @DisplayName("현재 링크 조회는 활성 링크가 없으면 빈 값을 반환한다")
    void getCurrentReturnsEmptyWhenNoActiveLink() {
        GroupRoom room = activeRoom();
        GroupRoomMember membership = ownerMembership();
        when(groupRoomRepository.findByIdAndStatusNot(GROUP_ID, GroupRoomStatus.DELETED))
                .thenReturn(Optional.of(room));
        when(groupRoomMemberRepository.findActiveMembershipInNotDeletedRoom(GROUP_ID, MEMBER_ID))
                .thenReturn(Optional.of(membership));
        when(groupInviteLinkRepository.findFirstByRoomIdAndExpiresAtAfterOrderByCreatedAtDescIdDesc(GROUP_ID, NOW))
                .thenReturn(Optional.empty());

        assertThat(manager.getCurrent(GROUP_ID, MEMBER_ID, NOW)).isEmpty();
    }

    @Test
    @DisplayName("만료된 초대 링크는 참여에 사용할 수 없다")
    void getJoinableRejectsExpiredLink() {
        GroupInviteLink expiredLink = new GroupInviteLink(mock(GroupRoom.class), TOKEN, NOW.minusSeconds(1));
        when(groupInviteLinkRepository.findByToken(TOKEN)).thenReturn(Optional.of(expiredLink));

        assertThatThrownBy(() -> manager.getJoinable(TOKEN, NOW))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.INVITE_LINK_EXPIRED);
    }

    @Test
    @DisplayName("고유 토큰을 5회 생성하지 못하면 발급을 중단한다")
    void createStopsAfterTokenCollisionLimit() {
        stubOwnerUpdateAccess();
        when(groupInviteLinkRepository.findFirstByRoomIdAndExpiresAtAfterOrderByCreatedAtDescIdDesc(GROUP_ID, NOW))
                .thenReturn(Optional.empty());
        when(tokenGenerator.generate()).thenReturn(TOKEN);
        when(groupInviteLinkRepository.existsByToken(TOKEN)).thenReturn(true);

        assertThatThrownBy(() -> manager.create(GROUP_ID, MEMBER_ID, NOW))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.INVITE_LINK_TOKEN_GENERATION_FAILED);
        verify(tokenGenerator, times(5)).generate();
    }

    private GroupRoom stubOwnerUpdateAccess() {
        GroupRoom room = activeRoom();
        GroupRoomMember membership = ownerMembership();
        when(groupRoomRepository.findByIdAndStatusNotForUpdate(GROUP_ID, GroupRoomStatus.DELETED))
                .thenReturn(Optional.of(room));
        when(groupRoomMemberRepository.findActiveMembershipInNotDeletedRoom(GROUP_ID, MEMBER_ID))
                .thenReturn(Optional.of(membership));
        return room;
    }

    private GroupRoom activeRoom() {
        GroupRoom room = mock(GroupRoom.class);
        when(room.getId()).thenReturn(GROUP_ID);
        when(room.isActive()).thenReturn(true);
        return room;
    }

    private GroupRoomMember ownerMembership() {
        GroupRoomMember membership = mock(GroupRoomMember.class);
        when(membership.isOwner()).thenReturn(true);
        return membership;
    }
}
