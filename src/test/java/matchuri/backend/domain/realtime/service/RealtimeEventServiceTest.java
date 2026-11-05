package matchuri.backend.domain.realtime.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.support.member.MemberReader;
import matchuri.backend.domain.realtime.entity.RealtimeEventType;
import matchuri.backend.domain.realtime.support.RealtimeSseEmitterRegistry;
import org.junit.jupiter.api.Test;

class RealtimeEventServiceTest {
    private final GroupRoomMemberRepository memberships = mock(GroupRoomMemberRepository.class);
    private final GroupRoomRepository rooms = mock(GroupRoomRepository.class);
    private final MemberRepository accounts = mock(MemberRepository.class);
    private final RealtimeSseEmitterRegistry registry = mock(RealtimeSseEmitterRegistry.class);
    private final RealtimeEventService service = new RealtimeEventService(
            mock(MemberReader.class), memberships, rooms, accounts, registry);

    @Test
    void groupRecipientsMustStillHaveActiveMembership() {
        when(rooms.findById(10L)).thenReturn(Optional.of(mock(GroupRoom.class)));
        List<GroupRoomMember> activeMemberships = List.of(membership(1L), membership(3L));
        when(memberships.findActiveMembersByRoomId(10L)).thenReturn(activeMemberships);
        service.sendToGroupMembers(10L, List.of(1L, 2L), RealtimeEventType.GROUP_MEMBER_JOINED, 10L, null, 1L, "payload");
        verify(registry).retainGroupConnections(10L, List.of(1L, 3L));
        verify(registry).sendToGroupMembers(eq(10L), eq(List.of(1L)), anyString(), eq("GROUP_MEMBER_JOINED"), any());
    }

    @Test
    void inactiveAccountReceivesNoPersonalEventAndConnectionsClose() {
        service.sendToMember(2L, RealtimeEventType.GROUP_INVITE_CREATED, 10L, null, 1L, "payload");
        verify(registry).completeMemberConnections(2L);
        verify(registry, never()).sendToMember(any(), anyString(), anyString(), any());
    }

    @Test
    void groupDeletionUsesActiveAccountsInSnapshotThenClosesGroupStream() {
        Member active = mock(Member.class);
        when(active.getId()).thenReturn(1L);
        when(active.getStatus()).thenReturn(MemberStatus.ACTIVE);
        Member inactive = mock(Member.class);
        when(inactive.getStatus()).thenReturn(MemberStatus.INACTIVE);
        when(accounts.findAllById(List.of(1L, 2L))).thenReturn(List.of(active, inactive));
        service.sendGroupDeletedToMembers(10L, List.of(1L, 2L), RealtimeEventType.GROUP_DELETED, 10L, null, 1L, "payload");
        var order = inOrder(registry);
        order.verify(registry).sendToGroupMembers(eq(10L), eq(List.of(1L)), anyString(), eq("GROUP_DELETED"), any());
        order.verify(registry).completeGroupConnections(10L);
        verifyNoInteractions(memberships, rooms);
    }

    @Test
    void deletedGroupConnectionsCloseEvenIfRecipientLookupFails() {
        when(accounts.findAllById(List.of(1L))).thenThrow(new IllegalStateException("lookup failed"));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.sendGroupDeletedToMembers(
                10L, List.of(1L), RealtimeEventType.GROUP_DELETED, 10L, null, 1L, "payload"))
                .isInstanceOf(IllegalStateException.class);
        verify(registry).completeGroupConnections(10L);
    }

    private GroupRoomMember membership(Long memberId) {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(memberId);
        GroupRoomMember membership = mock(GroupRoomMember.class);
        when(membership.getMember()).thenReturn(member);
        return membership;
    }
}
