package matchuri.backend.domain.realtime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import matchuri.backend.domain.group.entity.GroupInvite;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.repository.GroupInviteRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.realtime.entity.RealtimeEventType;
import matchuri.backend.domain.realtime.event.GroupInviteCreatedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupMemberJoinedRealtimeEvent;
import matchuri.backend.domain.realtime.result.GroupInviteCreatedRealtimePayload;
import matchuri.backend.domain.realtime.result.GroupMemberJoinedRealtimePayload;
import matchuri.backend.domain.realtime.result.RealtimeEventEnvelope;
import matchuri.backend.domain.realtime.support.RealtimeSseEmitterRegistry;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.ConfigurableTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionExecution;
import org.springframework.transaction.TransactionExecutionListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:realtime-event-transaction;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "matchuri.member-deletion.purge-cron=-"
})
@ActiveProfiles("test")
class RealtimeEventTransactionIntegrationTest {

    private static final LocalDateTime EVENT_TIME = LocalDateTime.of(2026, 11, 6, 12, 0);
    private static final LocalDateTime INVITE_EXPIRES_AT = LocalDateTime.of(2100, 1, 1, 0, 0);

    @Autowired MemberRepository members;
    @Autowired GroupRoomRepository rooms;
    @Autowired GroupInviteRepository invites;
    @Autowired ApplicationEventPublisher events;
    @Autowired PlatformTransactionManager transactions;
    @MockitoBean RealtimeSseEmitterRegistry registry;

    private ConfigurableTransactionManager configurableTransactions;
    private Collection<TransactionExecutionListener> originalTransactionListeners;

    @BeforeEach
    void rememberTransactionListeners() {
        configurableTransactions = (ConfigurableTransactionManager) transactions;
        originalTransactionListeners = new ArrayList<>(configurableTransactions.getTransactionExecutionListeners());
    }

    @AfterEach
    void cleanUp() {
        configurableTransactions.setTransactionExecutionListeners(originalTransactionListeners);
        invites.deleteAll();
        rooms.deleteAll();
        members.deleteAll();
    }

    @Test
    void committedGroupEventUsesFinalRecipientsAndSharesTheListenerReadTransaction() {
        List<Boolean> readOnlyTransactionBegins = new ArrayList<>();
        configurableTransactions.addListener(new TransactionExecutionListener() {
            @Override
            public void afterBegin(TransactionExecution transaction, @Nullable Throwable beginFailure) {
                readOnlyTransactionBegins.add(transaction.isReadOnly());
            }
        });
        AtomicReference<SendingTransaction> sendingTransaction = new AtomicReference<>();
        doAnswer(call -> {
            sendingTransaction.set(new SendingTransaction(
                    TransactionSynchronizationManager.isActualTransactionActive(),
                    TransactionSynchronizationManager.isCurrentTransactionReadOnly()));
            return null;
        }).when(registry).sendToGroupMembers(any(), any(), anyString(), anyString(), any());

        GroupFixture fixture = new TransactionTemplate(transactions).execute(status -> {
            Member owner = member("commit-owner");
            Member joined = member("commit-joined");
            Member departed = member("commit-departed");
            Member withdrawn = member("commit-withdrawn");
            GroupRoom room = GroupRoom.createOwnedBy("committed-group", "commit-group-code", owner);
            room.addGroupMember(joined, GroupMemberRole.MEMBER);
            room.addGroupMember(departed, GroupMemberRole.MEMBER);
            room.addGroupMember(withdrawn, GroupMemberRole.MEMBER);
            rooms.save(room);

            events.publishEvent(new GroupMemberJoinedRealtimeEvent(
                    room.getId(), joined.getId(), joined.getNickname(), EVENT_TIME));
            room.getGroupRoomMemberById(departed.getId()).orElseThrow().leave(EVENT_TIME);
            withdrawn.withdraw(EVENT_TIME, EVENT_TIME.plusDays(3));
            verifyNoInteractions(registry);
            return new GroupFixture(room.getId(), owner.getId(), joined.getId(), withdrawn.getId());
        });
        List<Boolean> observedTransactionBegins = List.copyOf(readOnlyTransactionBegins);

        assertThat(fixture).isNotNull();
        assertThat(observedTransactionBegins)
                .as("one publishing transaction and one shared listener/service read transaction")
                .containsExactly(false, true);
        assertThat(sendingTransaction.get()).isEqualTo(new SendingTransaction(true, true));
        ArgumentCaptor<Collection<Long>> recipients = ArgumentCaptor.captor();
        ArgumentCaptor<RealtimeEventEnvelope> envelope = ArgumentCaptor.forClass(RealtimeEventEnvelope.class);
        verify(registry).retainGroupConnections(fixture.groupId(), List.of(fixture.ownerId(), fixture.joinedId()));
        verify(registry).sendToGroupMembers(eq(fixture.groupId()), recipients.capture(), anyString(),
                eq("GROUP_MEMBER_JOINED"), envelope.capture());
        verifyNoMoreInteractions(registry);
        assertThat(recipients.getValue()).containsExactly(fixture.ownerId(), fixture.joinedId());
        assertThat(envelope.getValue().eventId()).isNotBlank();
        assertThat(envelope.getValue().eventType()).isEqualTo(RealtimeEventType.GROUP_MEMBER_JOINED);
        assertThat(envelope.getValue().groupId()).isEqualTo(fixture.groupId());
        assertThat(envelope.getValue().sessionId()).isNull();
        assertThat(envelope.getValue().actorMemberId()).isEqualTo(fixture.joinedId());
        assertThat(envelope.getValue().payload()).isEqualTo(new GroupMemberJoinedRealtimePayload(
                fixture.groupId(), fixture.joinedId(), "commit-joined", EVENT_TIME));
        assertThat(rooms.findById(fixture.groupId())).isPresent();
        assertThat(members.findById(fixture.withdrawnId()).orElseThrow().getStatus()).isEqualTo(MemberStatus.DELETED);
    }

    @Test
    void committedInviteIsDeliveredToItsMemberWithTheExpectedPayload() {
        InviteFixture fixture = new TransactionTemplate(transactions).execute(status -> {
            Member owner = member("invite-owner");
            Member target = member("invite-target");
            GroupRoom room = rooms.save(GroupRoom.createOwnedBy("invite-group", "invite-group-code", owner));
            GroupInvite invite = invites.save(new GroupInvite(room, owner, target, INVITE_EXPIRES_AT));
            events.publishEvent(inviteEvent(invite, room, owner, target));
            verifyNoInteractions(registry);
            return new InviteFixture(room.getId(), owner.getId(), target.getId(), invite.getId());
        });

        assertThat(fixture).isNotNull();
        ArgumentCaptor<RealtimeEventEnvelope> envelope = ArgumentCaptor.forClass(RealtimeEventEnvelope.class);
        verify(registry).sendToMember(eq(fixture.targetId()), anyString(), eq("GROUP_INVITE_CREATED"), envelope.capture());
        verifyNoMoreInteractions(registry);
        assertThat(envelope.getValue().eventId()).isNotBlank();
        assertThat(envelope.getValue().eventType()).isEqualTo(RealtimeEventType.GROUP_INVITE_CREATED);
        assertThat(envelope.getValue().groupId()).isEqualTo(fixture.groupId());
        assertThat(envelope.getValue().sessionId()).isNull();
        assertThat(envelope.getValue().actorMemberId()).isEqualTo(fixture.ownerId());
        assertThat(envelope.getValue().payload()).isEqualTo(new GroupInviteCreatedRealtimePayload(
                fixture.inviteId(), fixture.groupId(), "invite-group", fixture.ownerId(), "invite-owner", INVITE_EXPIRES_AT));
        assertThat(invites.findById(fixture.inviteId())).isPresent();
    }

    @Test
    void rollbackDiscardsEventsAndTheWritesThatWouldHaveMadeThemDeliverable() {
        InviteFixture fixture = new TransactionTemplate(transactions).execute(status -> {
            Member owner = member("rollback-owner");
            Member target = member("rollback-target");
            GroupRoom room = GroupRoom.createOwnedBy("rollback-group", "rollback-group-code", owner);
            room.addGroupMember(target, GroupMemberRole.MEMBER);
            rooms.save(room);
            GroupInvite invite = invites.save(new GroupInvite(room, owner, target, INVITE_EXPIRES_AT));
            events.publishEvent(inviteEvent(invite, room, owner, target));
            events.publishEvent(new GroupMemberJoinedRealtimeEvent(
                    room.getId(), target.getId(), target.getNickname(), EVENT_TIME));
            verifyNoInteractions(registry);
            status.setRollbackOnly();
            return new InviteFixture(room.getId(), owner.getId(), target.getId(), invite.getId());
        });

        assertThat(fixture).isNotNull();
        verifyNoInteractions(registry);
        assertThat(rooms.findById(fixture.groupId())).isEmpty();
        assertThat(invites.findById(fixture.inviteId())).isEmpty();
        assertThat(members.findById(fixture.ownerId())).isEmpty();
        assertThat(members.findById(fixture.targetId())).isEmpty();
    }

    private Member member(String loginId) {
        return members.save(Member.createWithEncodedPassword(loginId, "hashed-password", loginId, null));
    }

    private GroupInviteCreatedRealtimeEvent inviteEvent(GroupInvite invite, GroupRoom room, Member owner, Member target) {
        return new GroupInviteCreatedRealtimeEvent(invite.getId(), room.getId(), room.getName(),
                owner.getId(), owner.getNickname(), target.getId(), INVITE_EXPIRES_AT);
    }

    private record GroupFixture(Long groupId, Long ownerId, Long joinedId, Long withdrawnId) {
    }

    private record InviteFixture(Long groupId, Long ownerId, Long targetId, Long inviteId) {
    }

    private record SendingTransaction(boolean active, boolean readOnly) {
    }
}
