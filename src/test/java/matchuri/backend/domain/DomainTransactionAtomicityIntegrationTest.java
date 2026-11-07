package matchuri.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.List;
import matchuri.backend.domain.group.entity.GroupInvite;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.repository.GroupInviteRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.group.repository.impl.GroupRoomMemberRepositoryImpl;
import matchuri.backend.domain.group.result.GroupVoteProgressResult;
import matchuri.backend.domain.image.entity.ImageAsset;
import matchuri.backend.domain.image.entity.ImageStorageProvider;
import matchuri.backend.domain.image.entity.PresetProfileImage;
import matchuri.backend.domain.image.repository.ImageAssetRepository;
import matchuri.backend.domain.image.repository.PresetProfileImageRepository;
import matchuri.backend.domain.image.service.PresetProfileImageAdminService;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberProfileImage;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberProfileImageRepository;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.impl.MemberProfileImageRepositoryImpl;
import matchuri.backend.domain.member.support.deletion.MemberWithdrawalManager;
import matchuri.backend.domain.realtime.entity.RealtimeEventType;
import matchuri.backend.domain.realtime.event.GroupInviteCreatedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationVoteCompletedRealtimeEvent;
import matchuri.backend.domain.realtime.service.RealtimeDomainEventListener;
import matchuri.backend.domain.realtime.service.RealtimeEventService;
import matchuri.backend.domain.realtime.support.RealtimeSseEmitterRegistry;
import matchuri.backend.global.exception.BusinessException;
import matchuri.backend.infra.storage.ObjectStorageClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:domain-transaction-atomicity;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@ActiveProfiles("test")
class DomainTransactionAtomicityIntegrationTest {
    @Autowired MemberRepository members;
    @Autowired GroupInviteRepository invites;
    @Autowired RealtimeEventService realtime;
    @Autowired RealtimeDomainEventListener realtimeListener;
    @MockitoBean RealtimeSseEmitterRegistry registry;
    @Autowired GroupRoomRepository rooms;
    @Autowired ImageAssetRepository assets;
    @Autowired PresetProfileImageRepository presets;
    @Autowired MemberWithdrawalManager withdrawal;
    @Autowired PresetProfileImageAdminService presetService;
    @Autowired PlatformTransactionManager transactions;
    @MockitoBean ObjectStorageClient storage;
    @Autowired MemberProfileImageRepository profiles;
    @MockitoSpyBean GroupRoomMemberRepositoryImpl membershipQueries;
    @MockitoSpyBean MemberProfileImageRepositoryImpl profileQueries;

    @AfterEach
    void cleanUp() {
        rooms.deleteAll();
        profiles.deleteAll();
        members.deleteAll();
        presets.deleteAll();
        assets.deleteAll();
    }

    @Test
    void onlyCurrentPendingInvitesInExistingGroupsCanBeDelivered() {
        Member owner = member("invite-owner");
        Member target = member("invite-target");
        GroupRoom room = rooms.save(GroupRoom.createOwnedBy("invites", "invite-code", owner));
        GroupInvite pending = invites.save(new GroupInvite(room, owner, target, LocalDateTime.now().plusHours(1)));
        GroupInvite expired = invites.save(new GroupInvite(room, owner, target, LocalDateTime.now().minusHours(1)));
        realtimeListener.handle(inviteEvent(pending, room, owner, target));
        verify(registry).sendToMember(eq(target.getId()), anyString(), eq("GROUP_INVITE_CREATED"), any());
        clearInvocations(registry);
        realtimeListener.handle(inviteEvent(pending, room, owner, owner));
        verifyNoInteractions(registry);
        realtimeListener.handle(inviteEvent(expired, room, owner, target));
        verifyNoInteractions(registry);
        new TransactionTemplate(transactions).executeWithoutResult(status ->
                invites.findById(pending.getId()).orElseThrow().revoke());
        realtimeListener.handle(inviteEvent(pending, room, owner, target));
        verifyNoInteractions(registry);
        GroupInvite beforeDeletion = invites.save(new GroupInvite(room, owner, target, LocalDateTime.now().plusHours(1)));
        new TransactionTemplate(transactions).executeWithoutResult(status ->
                rooms.findById(room.getId()).orElseThrow().delete(LocalDateTime.now()));
        realtimeListener.handle(inviteEvent(beforeDeletion, room, owner, target));
        verifyNoInteractions(registry);
    }

    @Test
    void committedDeparturesWithdrawalsAndDeletionChangeEventRecipients() {
        Member owner = member("audience-owner");
        Member departing = member("audience-departing");
        Member withdrawn = member("audience-withdrawn");
        GroupRoom room = GroupRoom.createOwnedBy("audience", "audience-code", owner);
        room.addGroupMember(departing, GroupMemberRole.MEMBER);
        room.addGroupMember(withdrawn, GroupMemberRole.MEMBER);
        rooms.save(room);
        assertThat(groupRecipients(room.getId()))
                .containsExactlyInAnyOrder(owner.getId(), departing.getId(), withdrawn.getId());
        realtimeListener.handle(new GroupRecommendationVoteCompletedRealtimeEvent(
                room.getId(), 1L, owner.getId(), new GroupVoteProgressResult(3, 3)));
        verify(registry).sendToMember(eq(owner.getId()), anyString(), eq("GROUP_RECOMMENDATION_VOTE_COMPLETED"), any());
        clearInvocations(registry);
        realtimeListener.handle(new GroupRecommendationVoteCompletedRealtimeEvent(
                room.getId(), 1L, departing.getId(), new GroupVoteProgressResult(3, 3)));
        verifyNoInteractions(registry);
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            rooms.findById(room.getId()).orElseThrow().getGroupRoomMemberById(departing.getId())
                    .orElseThrow().leave(LocalDateTime.now());
            members.findById(withdrawn.getId()).orElseThrow()
                    .withdraw(LocalDateTime.now(), LocalDateTime.now().plusDays(3));
        });
        assertThat(groupRecipients(room.getId())).containsExactly(owner.getId());
        assertThatThrownBy(() -> realtime.connectGroupStream(departing.getId(), room.getId()))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(registry);
        realtime.sendToMember(withdrawn.getId(), RealtimeEventType.GROUP_INVITE_CREATED,
                room.getId(), null, owner.getId(), "payload");
        verify(registry).completeMemberConnections(withdrawn.getId());
        org.mockito.Mockito.verifyNoMoreInteractions(registry);
        clearInvocations(registry);
        realtime.sendGroupDeletedToMembers(room.getId(), List.of(owner.getId(), withdrawn.getId()),
                RealtimeEventType.GROUP_DELETED, room.getId(), null, owner.getId(), "payload");
        ArgumentCaptor<java.util.Collection<Long>> targets = ArgumentCaptor.captor();
        verify(registry).sendToGroupMembers(eq(room.getId()), targets.capture(), anyString(), eq("GROUP_DELETED"), any());
        assertThat(targets.getValue()).containsExactly(owner.getId());
        verify(registry).completeGroupConnections(room.getId());
        clearInvocations(registry);
        new TransactionTemplate(transactions).executeWithoutResult(status ->
                rooms.findById(room.getId()).orElseThrow().delete(LocalDateTime.now()));
        assertThat(groupRecipients(room.getId())).isEmpty();
        realtimeListener.handle(new GroupRecommendationVoteCompletedRealtimeEvent(
                room.getId(), 1L, owner.getId(), new GroupVoteProgressResult(1, 1)));
        verifyNoInteractions(registry);
    }

    private List<Long> groupRecipients(Long groupId) {
        realtime.sendToGroup(groupId, RealtimeEventType.GROUP_MEMBER_JOINED, groupId, null, null, "payload");
        ArgumentCaptor<java.util.Collection<Long>> targets = ArgumentCaptor.captor();
        verify(registry).sendToGroupMembers(eq(groupId), targets.capture(), anyString(), eq("GROUP_MEMBER_JOINED"), any());
        List<Long> recipients = List.copyOf(targets.getValue());
        clearInvocations(registry);
        return recipients;
    }

    private GroupInviteCreatedRealtimeEvent inviteEvent(GroupInvite invite, GroupRoom room, Member owner, Member target) {
        return new GroupInviteCreatedRealtimeEvent(invite.getId(), room.getId(), room.getName(),
                owner.getId(), owner.getNickname(), target.getId(), invite.getExpiresAt());
    }

    @Test
    void withdrawalAndOwnedGroupDeletionCommitTogether() {
        Member member = member("atomic-withdraw");
        GroupRoom room = rooms.save(GroupRoom.createOwnedBy("group", "atomic-code", member));
        new TransactionTemplate(transactions).executeWithoutResult(status -> withdrawal.withdraw(member.getId(), LocalDateTime.now()));
        assertThat(members.findById(member.getId()).orElseThrow().getStatus()).isEqualTo(MemberStatus.DELETED);
        assertThat(rooms.findById(room.getId()).orElseThrow().getStatus()).isEqualTo(GroupRoomStatus.DELETED);
    }

    @Test
    void failingWithdrawalCleanupRollsBackMemberAndOwnedGroupChanges() {
        Member member = member("failed-withdraw");
        GroupRoom room = rooms.save(GroupRoom.createOwnedBy("group", "failed-code", member));
        doAnswer(call -> { call.callRealMethod(); throw new IllegalStateException("cleanup failed"); })
                .when(membershipQueries).findActiveMembershipsInOtherRooms(any());
        assertThatThrownBy(() -> new TransactionTemplate(transactions).executeWithoutResult(
                status -> withdrawal.withdraw(member.getId(), LocalDateTime.now())))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("cleanup failed");
        assertThat(members.findById(member.getId()).orElseThrow().getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(rooms.findById(room.getId()).orElseThrow().getStatus()).isEqualTo(GroupRoomStatus.ACTIVE);
    }

    @Test
    void presetDeletionReassignsAllUsersBeforeSuccess() {
        PresetProfileImage fallback = preset("fallback.png", true);
        PresetProfileImage deleted = preset("selected.png", false);
        profiles.save(new MemberProfileImage(member("first-profile"), deleted.getImageAsset()));
        profiles.save(new MemberProfileImage(member("second-profile"), deleted.getImageAsset()));
        presetService.delete(deleted.getId());
        assertThat(presets.findById(deleted.getId()).orElseThrow().isDeleted()).isTrue();
        assertThat(profiles.findAll()).hasSize(2).allSatisfy(profile ->
                assertThat(profile.getImageAsset().getId()).isEqualTo(fallback.getImageAsset().getId()));
    }

    @Test
    void failingProfileReassignmentRollsBackPresetAndProfileChanges() {
        preset("fallback.png", true);
        PresetProfileImage selected = preset("selected.png", false);
        Member member = member("failed-profile");
        profiles.save(new MemberProfileImage(member, selected.getImageAsset()));
        doAnswer(call -> { call.callRealMethod(); throw new IllegalStateException("profile update failed"); })
                .when(profileQueries).updateToDefault(any(), any());
        assertThatThrownBy(() -> presetService.delete(selected.getId()))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("profile update failed");
        assertThat(presets.findById(selected.getId()).orElseThrow().isDeleted()).isFalse();
        assertThat(profiles.findByMemberId(member.getId()).orElseThrow().getImageAsset().getId())
                .isEqualTo(selected.getImageAsset().getId());
    }

    private Member member(String loginId) {
        return members.save(new Member(loginId, "hashed-password", null, false, null, null,
                MemberRole.MEMBER, MemberStatus.ACTIVE));
    }

    private PresetProfileImage preset(String key, boolean isDefault) {
        ImageAsset asset = assets.save(new ImageAsset(ImageStorageProvider.CLOUDFLARE_R2,
                "test-bucket", key, key, "image/png", 1024, "a".repeat(64), 320, 320));
        return presets.save(new PresetProfileImage(asset, isDefault));
    }
}
