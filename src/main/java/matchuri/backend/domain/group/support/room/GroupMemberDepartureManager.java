package matchuri.backend.domain.group.support.room;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.realtime.event.GroupMemberLeftRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationOpenedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationVoteCompletedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationVoteUpdatedRealtimeEvent;
import matchuri.backend.domain.group.repository.GroupRecommendationRepository;
import matchuri.backend.domain.group.repository.GroupRecommendationVoteRepository;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.result.GroupRecommendationReadinessProgressResult;
import matchuri.backend.domain.group.result.GroupVoteProgressResult;
import matchuri.backend.domain.group.support.recommendation.GroupRecommendationExpirationManager;
import matchuri.backend.domain.group.support.recommendation.GroupRecommendationOpeningManager;
import matchuri.backend.domain.group.support.recommendation.GroupRecommendationResultAssembler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupMemberDepartureManager {

    private static final List<GroupRecommendationStatus> ACTIVE_STATUSES = List.of(
            GroupRecommendationStatus.PREPARING,
            GroupRecommendationStatus.OPEN
    );

    private final GroupRecommendationRepository recommendationRepository;
    private final GroupRecommendationVoteRepository voteRepository;
    private final GroupRoomMemberRepository membershipRepository;
    private final GroupRecommendationExpirationManager expirationManager;
    private final GroupRecommendationOpeningManager openingManager;
    private final GroupRecommendationResultAssembler resultAssembler;
    private final ApplicationEventPublisher eventPublisher;

    public void leave(GroupRoomMember membership, LocalDateTime leftAt) {
        GroupRoom room = membership.getRoom();
        Long memberId = membership.getMember().getId();
        membership.leave(leftAt);

        recommendationRepository.findFirstByRoomIdAndStatusInOrderByCreatedAtDescIdDesc(room.getId(), ACTIVE_STATUSES)
                .ifPresent(recommendation -> updateRecommendation(room, recommendation, memberId, leftAt));

        eventPublisher.publishEvent(new GroupMemberLeftRealtimeEvent(
                room.getId(), memberId, membership.getMember().getNickname(), leftAt
        ));
    }

    private void updateRecommendation(
            GroupRoom room,
            GroupRecommendation recommendation,
            Long memberId,
            LocalDateTime leftAt
    ) {
        if (expirationManager.expireGroupRecommendationIfNeeded(recommendation, leftAt)) {
            return;
        }

        if (recommendation.getStatus() == GroupRecommendationStatus.PREPARING) {
            int totalMemberCount = membershipRepository.findActiveMembersByRoomId(room.getId()).size();
            GroupRecommendationReadinessProgressResult readiness = resultAssembler.readinessProgress(
                    recommendation.getId(), room.getId(), totalMemberCount
            );
            if (readiness.allReady()) {
                var candidates = openingManager.open(room, recommendation);
                eventPublisher.publishEvent(new GroupRecommendationOpenedRealtimeEvent(
                        room.getId(), recommendation.getId(), recommendation.getStatus(), candidates,
                        resultAssembler.toVoteProgress(recommendation)
                ));
            }
            return;
        }

        voteRepository.deleteByGroupRecommendationIdAndMemberId(recommendation.getId(), memberId);
        GroupVoteProgressResult progress = resultAssembler.toVoteProgress(recommendation);
        eventPublisher.publishEvent(new GroupRecommendationVoteUpdatedRealtimeEvent(
                room.getId(), recommendation.getId(), progress
        ));
        if (progress.totalMemberCount() > 0
                && progress.totalMemberCount().equals(progress.votedMemberCount())) {
            eventPublisher.publishEvent(new GroupRecommendationVoteCompletedRealtimeEvent(
                    room.getId(), recommendation.getId(), room.getHostMember().getId(), progress
            ));
        }
    }
}
