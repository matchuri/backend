package matchuri.backend.domain.realtime.service;

import java.util.List;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.repository.GroupInviteRepository;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.realtime.entity.RealtimeEventType;
import matchuri.backend.domain.realtime.event.GroupDeletedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupInviteCreatedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupMemberJoinedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupMemberLeftRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationFinalizedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationOpenedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationReadinessUpdatedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationStartedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationVoteCompletedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationVoteUpdatedRealtimeEvent;
import matchuri.backend.domain.realtime.result.GroupDeletedRealtimePayload;
import matchuri.backend.domain.realtime.result.GroupInviteCreatedRealtimePayload;
import matchuri.backend.domain.realtime.result.GroupMemberJoinedRealtimePayload;
import matchuri.backend.domain.realtime.result.GroupMemberLeftRealtimePayload;
import matchuri.backend.domain.realtime.result.GroupRecommendationFinalizedRealtimePayload;
import matchuri.backend.domain.realtime.result.GroupRecommendationOpenedRealtimePayload;
import matchuri.backend.domain.realtime.result.GroupRecommendationReadinessUpdatedRealtimePayload;
import matchuri.backend.domain.realtime.result.GroupRecommendationStartedRealtimePayload;
import matchuri.backend.domain.realtime.result.GroupRecommendationVoteCompletedRealtimePayload;
import matchuri.backend.domain.realtime.result.GroupRecommendationVoteUpdatedRealtimePayload;
import matchuri.backend.domain.realtime.result.RealtimeCandidatePayload;
import matchuri.backend.domain.realtime.result.RealtimeReadinessProgressPayload;
import matchuri.backend.domain.realtime.result.RealtimeVoteProgressPayload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
public class RealtimeDomainEventListener {

    private final RealtimeEventService realtimeEventService;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupRoomRepository groupRoomRepository;
    private final GroupInviteRepository groupInviteRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GroupInviteCreatedRealtimeEvent event) {
        if (groupInviteRepository.findById(event.inviteId())
                .filter(invite -> invite.isPending() && !invite.isExpired(LocalDateTime.now())
                        && invite.getRoom().getStatus() != GroupRoomStatus.DELETED
                        && invite.getTargetMember().getId().equals(event.targetMemberId())).isEmpty()) {
            return;
        }
        realtimeEventService.sendToMember(
                event.targetMemberId(),
                RealtimeEventType.GROUP_INVITE_CREATED,
                event.groupId(),
                null,
                event.requestMemberId(),
                new GroupInviteCreatedRealtimePayload(
                        event.inviteId(),
                        event.groupId(),
                        event.groupName(),
                        event.requestMemberId(),
                        event.requestMemberNickname(),
                        event.expiresAt()
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GroupMemberJoinedRealtimeEvent event) {
        realtimeEventService.sendToGroupMembers(
                event.groupId(),
                activeMemberIds(event.groupId()),
                RealtimeEventType.GROUP_MEMBER_JOINED,
                event.groupId(),
                null,
                event.memberId(),
                new GroupMemberJoinedRealtimePayload(
                        event.groupId(),
                        event.memberId(),
                        event.memberNickname(),
                        event.joinedAt()
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GroupMemberLeftRealtimeEvent event) {
        realtimeEventService.sendToGroupMembers(
                event.groupId(),
                activeMemberIds(event.groupId()),
                RealtimeEventType.GROUP_MEMBER_LEFT,
                event.groupId(),
                null,
                event.memberId(),
                new GroupMemberLeftRealtimePayload(
                        event.groupId(),
                        event.memberId(),
                        event.memberNickname(),
                        event.leftAt()
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GroupDeletedRealtimeEvent event) {
        realtimeEventService.sendGroupDeletedToMembers(
                event.groupId(),
                event.targetMemberIds(),
                RealtimeEventType.GROUP_DELETED,
                event.groupId(),
                null,
                event.deletedByMemberId(),
                new GroupDeletedRealtimePayload(
                        event.groupId(),
                        event.deletedByMemberId(),
                        event.deletedAt()
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GroupRecommendationStartedRealtimeEvent event) {
        realtimeEventService.sendToGroupMembers(
                event.groupId(),
                activeMemberIds(event.groupId()),
                RealtimeEventType.GROUP_RECOMMENDATION_STARTED,
                event.groupId(),
                event.sessionId(),
                event.actorMemberId(),
                new GroupRecommendationStartedRealtimePayload(
                        event.sessionId(),
                        event.status(),
                        RealtimeReadinessProgressPayload.from(event.readinessProgress())
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GroupRecommendationReadinessUpdatedRealtimeEvent event) {
        realtimeEventService.sendToGroupMembers(
                event.groupId(),
                activeMemberIds(event.groupId()),
                RealtimeEventType.GROUP_RECOMMENDATION_READINESS_UPDATED,
                event.groupId(),
                event.sessionId(),
                event.readyMemberId(),
                new GroupRecommendationReadinessUpdatedRealtimePayload(
                        event.sessionId(),
                        event.status(),
                        event.readyMemberId(),
                        event.readyMemberNickname(),
                        RealtimeReadinessProgressPayload.from(event.readinessProgress())
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GroupRecommendationOpenedRealtimeEvent event) {
        realtimeEventService.sendToGroupMembers(
                event.groupId(),
                activeMemberIds(event.groupId()),
                RealtimeEventType.GROUP_RECOMMENDATION_OPENED,
                event.groupId(),
                event.sessionId(),
                null,
                new GroupRecommendationOpenedRealtimePayload(
                        event.sessionId(),
                        event.status(),
                        event.candidates().stream()
                                .map(RealtimeCandidatePayload::from)
                                .toList(),
                        RealtimeVoteProgressPayload.from(event.voteProgress())
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GroupRecommendationVoteUpdatedRealtimeEvent event) {
        realtimeEventService.sendToGroupMembers(
                event.groupId(),
                activeMemberIds(event.groupId()),
                RealtimeEventType.GROUP_RECOMMENDATION_VOTE_UPDATED,
                event.groupId(),
                event.sessionId(),
                null,
                new GroupRecommendationVoteUpdatedRealtimePayload(
                        event.sessionId(),
                        RealtimeVoteProgressPayload.from(event.voteProgress())
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GroupRecommendationVoteCompletedRealtimeEvent event) {
        if (groupRoomMemberRepository.findActiveMembershipInNotDeletedRoom(event.groupId(), event.ownerMemberId())
                .filter(membership -> membership.isOwner()
                        && membership.getMember().getStatus() == MemberStatus.ACTIVE).isEmpty()) {
            return;
        }
        realtimeEventService.sendToMember(
                event.ownerMemberId(),
                RealtimeEventType.GROUP_RECOMMENDATION_VOTE_COMPLETED,
                event.groupId(),
                event.sessionId(),
                null,
                new GroupRecommendationVoteCompletedRealtimePayload(
                        event.sessionId(),
                        RealtimeVoteProgressPayload.from(event.voteProgress()),
                        true
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GroupRecommendationFinalizedRealtimeEvent event) {
        realtimeEventService.sendToGroupMembers(
                event.groupId(),
                activeMemberIds(event.groupId()),
                RealtimeEventType.GROUP_RECOMMENDATION_FINALIZED,
                event.groupId(),
                event.sessionId(),
                event.actorMemberId(),
                new GroupRecommendationFinalizedRealtimePayload(
                        event.sessionId(),
                        event.status(),
                        event.finalizedAt(),
                        RealtimeCandidatePayload.from(event.finalCandidate())
                )
        );
    }

    private List<Long> activeMemberIds(Long groupId) {
        if (groupRoomRepository.findById(groupId)
                .filter(room -> room.getStatus() != GroupRoomStatus.DELETED).isEmpty()) {
            return List.of();
        }
        return groupRoomMemberRepository.findActiveMembersByRoomId(groupId)
                .stream()
                .map(groupMember -> groupMember.getMember().getId())
                .toList();
    }
}
