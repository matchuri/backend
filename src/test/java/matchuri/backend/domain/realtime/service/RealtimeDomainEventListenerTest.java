package matchuri.backend.domain.realtime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.result.GroupVoteProgressResult;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.realtime.entity.RealtimeEventType;
import matchuri.backend.domain.realtime.event.GroupDeletedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupMemberJoinedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupMemberLeftRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationVoteCompletedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationVoteUpdatedRealtimeEvent;
import matchuri.backend.domain.realtime.result.GroupDeletedRealtimePayload;
import matchuri.backend.domain.realtime.result.GroupMemberJoinedRealtimePayload;
import matchuri.backend.domain.realtime.result.GroupMemberLeftRealtimePayload;
import matchuri.backend.domain.realtime.result.GroupRecommendationVoteCompletedRealtimePayload;
import matchuri.backend.domain.realtime.result.GroupRecommendationVoteUpdatedRealtimePayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RealtimeDomainEventListenerTest {

    private final RealtimeEventService realtimeEventService = mock(RealtimeEventService.class);
    private final GroupRoomMemberRepository groupRoomMemberRepository = mock(GroupRoomMemberRepository.class);
    private final RealtimeDomainEventListener listener =
            new RealtimeDomainEventListener(realtimeEventService, groupRoomMemberRepository);

    @Test
    @DisplayName("멤버 참여 이벤트는 그룹 활성 멤버에게 전송한다")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void sendsMemberJoinedToActiveGroupMembers() {
        Long groupId = 3001L;
        LocalDateTime joinedAt = LocalDateTime.of(2026, 6, 3, 12, 0);
        GroupRoomMember ownerMembership = mockMembership(1001L);
        GroupRoomMember memberMembership = mockMembership(1002L);

        when(groupRoomMemberRepository.findActiveMembersByRoomId(groupId))
                .thenReturn(List.of(ownerMembership, memberMembership));

        listener.handle(new GroupMemberJoinedRealtimeEvent(
                groupId,
                1002L,
                "member",
                joinedAt
        ));

        ArgumentCaptor<Iterable<Long>> memberIdsCaptor = ArgumentCaptor.forClass(Iterable.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        verify(realtimeEventService).sendToGroupMembers(
                eq(groupId),
                memberIdsCaptor.capture(),
                eq(RealtimeEventType.GROUP_MEMBER_JOINED),
                eq(groupId),
                eq(null),
                eq(1002L),
                payloadCaptor.capture()
        );

        assertThat(memberIdsCaptor.getValue()).containsExactly(1001L, 1002L);
        assertThat(payloadCaptor.getValue())
                .isInstanceOfSatisfying(GroupMemberJoinedRealtimePayload.class, payload -> {
                    assertThat(payload.groupId()).isEqualTo(groupId);
                    assertThat(payload.memberId()).isEqualTo(1002L);
                    assertThat(payload.memberNickname()).isEqualTo("member");
                    assertThat(payload.joinedAt()).isEqualTo(joinedAt);
                });
    }

    @Test
    @DisplayName("멤버 탈퇴 이벤트는 남아 있는 그룹 활성 멤버에게 전송한다")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void sendsMemberLeftToActiveGroupMembers() {
        Long groupId = 3001L;
        LocalDateTime leftAt = LocalDateTime.of(2026, 6, 3, 12, 5);
        GroupRoomMember ownerMembership = mockMembership(1001L);

        when(groupRoomMemberRepository.findActiveMembersByRoomId(groupId))
                .thenReturn(List.of(ownerMembership));

        listener.handle(new GroupMemberLeftRealtimeEvent(
                groupId,
                1002L,
                "member",
                leftAt
        ));

        ArgumentCaptor<Iterable<Long>> memberIdsCaptor = ArgumentCaptor.forClass(Iterable.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        verify(realtimeEventService).sendToGroupMembers(
                eq(groupId),
                memberIdsCaptor.capture(),
                eq(RealtimeEventType.GROUP_MEMBER_LEFT),
                eq(groupId),
                eq(null),
                eq(1002L),
                payloadCaptor.capture()
        );

        assertThat(memberIdsCaptor.getValue()).containsExactly(1001L);
        assertThat(payloadCaptor.getValue())
                .isInstanceOfSatisfying(GroupMemberLeftRealtimePayload.class, payload -> {
                    assertThat(payload.groupId()).isEqualTo(groupId);
                    assertThat(payload.memberId()).isEqualTo(1002L);
                    assertThat(payload.memberNickname()).isEqualTo("member");
                    assertThat(payload.leftAt()).isEqualTo(leftAt);
                });
    }

    @Test
    @DisplayName("그룹 삭제 이벤트는 삭제 직전 대상 멤버에게 전송한다")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void sendsGroupDeletedToSnapshotTargetMembers() {
        Long groupId = 3001L;
        Long ownerMemberId = 1001L;
        LocalDateTime deletedAt = LocalDateTime.of(2026, 6, 3, 12, 10);

        listener.handle(new GroupDeletedRealtimeEvent(
                groupId,
                ownerMemberId,
                List.of(1001L, 1002L),
                deletedAt
        ));

        ArgumentCaptor<Iterable<Long>> memberIdsCaptor = ArgumentCaptor.forClass(Iterable.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        verify(realtimeEventService).sendToGroupMembers(
                eq(groupId),
                memberIdsCaptor.capture(),
                eq(RealtimeEventType.GROUP_DELETED),
                eq(groupId),
                eq(null),
                eq(ownerMemberId),
                payloadCaptor.capture()
        );

        assertThat(memberIdsCaptor.getValue()).containsExactly(1001L, 1002L);
        assertThat(payloadCaptor.getValue())
                .isInstanceOfSatisfying(GroupDeletedRealtimePayload.class, payload -> {
                    assertThat(payload.groupId()).isEqualTo(groupId);
                    assertThat(payload.deletedByMemberId()).isEqualTo(ownerMemberId);
                    assertThat(payload.deletedAt()).isEqualTo(deletedAt);
                });
    }

    @Test
    @DisplayName("투표 갱신 이벤트는 그룹 활성 멤버에게 진행률만 전송한다")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void sendsVoteProgressOnlyToActiveGroupMembers() {
        Long groupId = 3001L;
        Long sessionId = 5001L;
        GroupRoomMember ownerMembership = mockMembership(1001L);
        GroupRoomMember memberMembership = mockMembership(1002L);

        when(groupRoomMemberRepository.findActiveMembersByRoomId(groupId))
                .thenReturn(List.of(ownerMembership, memberMembership));

        listener.handle(new GroupRecommendationVoteUpdatedRealtimeEvent(
                groupId,
                sessionId,
                new GroupVoteProgressResult(2, 1)
        ));

        ArgumentCaptor<Iterable<Long>> memberIdsCaptor = ArgumentCaptor.forClass(Iterable.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        verify(realtimeEventService).sendToGroupMembers(
                eq(groupId),
                memberIdsCaptor.capture(),
                eq(RealtimeEventType.GROUP_RECOMMENDATION_VOTE_UPDATED),
                eq(groupId),
                eq(sessionId),
                eq(null),
                payloadCaptor.capture()
        );

        assertThat(memberIdsCaptor.getValue()).containsExactly(1001L, 1002L);
        assertThat(payloadCaptor.getValue())
                .isInstanceOfSatisfying(GroupRecommendationVoteUpdatedRealtimePayload.class, payload -> {
                    assertThat(payload.sessionId()).isEqualTo(sessionId);
                    assertThat(payload.voteProgress().totalMemberCount()).isEqualTo(2);
                    assertThat(payload.voteProgress().votedMemberCount()).isEqualTo(1);
                    assertThat(payload.voteProgress().allVoted()).isFalse();
                });
    }

    @Test
    @DisplayName("전원 투표 완료 이벤트는 그룹장에게 수동 확정 필요 상태를 전송한다")
    void sendsVoteCompletedOnlyToOwner() {
        Long groupId = 3001L;
        Long sessionId = 5001L;
        Long ownerMemberId = 1001L;

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        listener.handle(new GroupRecommendationVoteCompletedRealtimeEvent(
                groupId,
                sessionId,
                ownerMemberId,
                new GroupVoteProgressResult(2, 2)
        ));

        verify(realtimeEventService).sendToMember(
                eq(ownerMemberId),
                eq(RealtimeEventType.GROUP_RECOMMENDATION_VOTE_COMPLETED),
                eq(groupId),
                eq(sessionId),
                eq(null),
                payloadCaptor.capture()
        );

        assertThat(payloadCaptor.getValue())
                .isInstanceOfSatisfying(GroupRecommendationVoteCompletedRealtimePayload.class, payload -> {
                    assertThat(payload.sessionId()).isEqualTo(sessionId);
                    assertThat(payload.voteProgress().totalMemberCount()).isEqualTo(2);
                    assertThat(payload.voteProgress().votedMemberCount()).isEqualTo(2);
                    assertThat(payload.voteProgress().allVoted()).isTrue();
                    assertThat(payload.finalizeRequired()).isTrue();
                });
    }

    private GroupRoomMember mockMembership(Long memberId) {
        Member member = mock(Member.class);
        GroupRoomMember membership = mock(GroupRoomMember.class);

        when(member.getId()).thenReturn(memberId);
        when(membership.getMember()).thenReturn(member);

        return membership;
    }
}
