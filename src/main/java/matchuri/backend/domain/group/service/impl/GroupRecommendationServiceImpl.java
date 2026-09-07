package matchuri.backend.domain.group.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.command.CreateGroupRecommendationCommand;
import matchuri.backend.domain.group.command.FinalizeGroupRecommendationCommand;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationCandidate;
import matchuri.backend.domain.group.entity.GroupRecommendationReadiness;
import matchuri.backend.domain.group.entity.GroupRecommendationRerollType;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import matchuri.backend.domain.group.entity.GroupRecommendationVote;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.exception.GroupErrorCode;
import matchuri.backend.domain.group.repository.GroupRecommendationCandidateRepository;
import matchuri.backend.domain.group.repository.GroupRecommendationReadinessRepository;
import matchuri.backend.domain.group.repository.GroupRecommendationRepository;
import matchuri.backend.domain.group.repository.GroupRecommendationVoteRepository;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.result.CreateGroupRecommendationResult;
import matchuri.backend.domain.group.result.FinalizeGroupRecommendationResult;
import matchuri.backend.domain.group.result.GroupHomeActivityResult;
import matchuri.backend.domain.group.result.GroupRecommendationCandidateListResult;
import matchuri.backend.domain.group.result.GroupRecommendationCandidateResult;
import matchuri.backend.domain.group.result.GroupRecommendationReadinessMemberResult;
import matchuri.backend.domain.group.result.GroupRecommendationReadinessProgressResult;
import matchuri.backend.domain.group.result.GroupRecommendationReadinessResult;
import matchuri.backend.domain.group.result.GroupRecommendationResult;
import matchuri.backend.domain.group.result.GroupRecommendationSummaryResult;
import matchuri.backend.domain.group.result.GroupVoteProgressResult;
import matchuri.backend.domain.group.result.GroupVoteResult;
import matchuri.backend.domain.group.result.ReadyGroupRecommendationResult;
import matchuri.backend.domain.group.service.GroupRecommendationService;
import matchuri.backend.domain.group.support.GroupFinalCandidateSelector;
import matchuri.backend.domain.group.support.location.GroupLocationManager;
import matchuri.backend.domain.group.support.recommendation.GroupRecommendationCandidateGenerator;
import matchuri.backend.domain.group.support.recommendation.GroupRecommendationExpirationManager;
import matchuri.backend.domain.group.support.recommendation.GroupRecommendationHistoryReader;
import matchuri.backend.domain.group.support.recommendation.GroupRecommendationResultAssembler;
import matchuri.backend.domain.group.support.room.GroupRoomReader;
import matchuri.backend.domain.recommendation.context.RecommendationLocationContextJsonFactory;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.support.member.MemberReader;
import matchuri.backend.domain.realtime.event.GroupRecommendationFinalizedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationOpenedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationReadinessUpdatedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationStartedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationVoteCompletedRealtimeEvent;
import matchuri.backend.domain.realtime.event.GroupRecommendationVoteUpdatedRealtimeEvent;
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
public class GroupRecommendationServiceImpl implements GroupRecommendationService {

    private final MemberReader memberReader;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupRecommendationRepository groupRecommendationRepository;
    private final GroupRecommendationCandidateRepository groupRecommendationCandidateRepository;
    private final GroupRecommendationReadinessRepository groupRecommendationReadinessRepository;
    private final GroupRecommendationVoteRepository groupRecommendationVoteRepository;
    private final GroupFinalCandidateSelector groupFinalCandidateSelector;
    private final GroupRoomReader groupRoomReader;
    private final GroupLocationManager groupLocationManager;
    private final GroupRecommendationExpirationManager groupRecommendationExpirationManager;
    private final GroupRecommendationHistoryReader groupRecommendationHistoryReader;
    private final GroupRecommendationCandidateGenerator groupRecommendationCandidateGenerator;
    private final GroupRecommendationResultAssembler groupRecommendationResultAssembler;
    private final RecommendationLocationContextJsonFactory recommendationLocationContextJsonFactory;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CreateGroupRecommendationResult createGroupRecommendation(
            Long memberId,
            CreateGroupRecommendationCommand command
    ) {
        Member member = memberReader.getActiveMember(memberId);
        GroupRoom room = groupRoomReader.getActiveGroupRoom(command.groupId());

        if (!groupRoomReader.getActiveMembership(room.getId(), member.getId()).isOwner()) {
            throw new BusinessException(GroupErrorCode.RECOMMENDATION_CREATE_FORBIDDEN, room.getId());
        }

        groupRecommendationExpirationManager.expireActiveGroupRecommendations(room.getId(), LocalDateTime.now());

        if (groupRecommendationExpirationManager.hasActiveRecommendation(room.getId())) {
            throw new BusinessException(GroupErrorCode.RECOMMENDATION_ACTIVE_EXISTS, room.getId());
        }

        groupLocationManager.updateLatestGroupLocation(
                room,
                command.latitude(),
                command.longitude(),
                command.radiusMeters(),
                command.address()
        );

        GroupRecommendation recommendation = groupRecommendationRepository.save(
                GroupRecommendation.preparing(room)
        );

        int totalMemberCount = groupRoomMemberRepository.findActiveMembersByRoomId(room.getId()).size();
        GroupRecommendationReadinessProgressResult readiness =
                GroupRecommendationReadinessProgressResult.of(totalMemberCount, 0);

        eventPublisher.publishEvent(new GroupRecommendationStartedRealtimeEvent(
                room.getId(),
                recommendation.getId(),
                member.getId(),
                recommendation.getStatus(),
                readiness
        ));

        return new CreateGroupRecommendationResult(
                recommendation.getId(),
                recommendation.getStatus(),
                List.of()
        );
    }

    @Override
    public CreateGroupRecommendationResult rerollGroupRecommendation(
            Long memberId,
            Long groupId,
            Long sessionId,
            GroupRecommendationRerollType rerollType,
            String contextJson
    ) {
        throw new BusinessException(GroupErrorCode.RECOMMENDATION_REROLL_DISABLED);
    }

    @Override
    public GroupRecommendationResult getGroupRecommendation(Long memberId, Long groupId, Long sessionId) {
        Member member = memberReader.getActiveMember(memberId);
        groupRoomReader.getActiveMembership(groupId, member.getId());

        GroupRecommendation recommendation = groupRecommendationRepository.findByIdAndRoomId(sessionId, groupId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.RECOMMENDATION_NOT_FOUND, sessionId));
        groupRecommendationExpirationManager.expireGroupRecommendationIfNeeded(recommendation, LocalDateTime.now());
        List<GroupRoomMember> activeMemberships = groupRoomMemberRepository.findActiveMembersByRoomId(groupId);

        return groupRecommendationResultAssembler.toGroupRecommendationResult(
                recommendation,
                member.getId(),
                activeMemberships
        );
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public GroupRecommendationCandidateListResult getGroupRecommendationCandidates(
            Long memberId,
            Long groupId,
            Long sessionId
    ) {
        Member member = memberReader.getActiveMember(memberId);
        groupRoomReader.getActiveMembership(groupId, member.getId());

        GroupRecommendation recommendation = groupRecommendationRepository.findByIdAndRoomId(sessionId, groupId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.RECOMMENDATION_NOT_FOUND, sessionId));

        validateGroupRecommendationOpen(recommendation, sessionId);

        return new GroupRecommendationCandidateListResult(
                recommendation.getId(),
                groupRecommendationResultAssembler.toCandidateResults(recommendation)
        );
    }

    @Override
    public Page<@NonNull GroupRecommendationSummaryResult> getGroupRecommendations(
            Long memberId,
            Long groupId,
            int page,
            int size
    ) {
        Member member = memberReader.getActiveMember(memberId);
        GroupRoom room = groupRoomReader.getActiveGroupRoom(groupId);
        groupRoomReader.getActiveMembership(room.getId(), member.getId());

        groupRecommendationExpirationManager.expireActiveGroupRecommendations(room.getId(), LocalDateTime.now());

        return groupRecommendationRepository
                .findByRoomIdOrderByCreatedAtDescIdDesc(room.getId(), PageRequest.of(page, size))
                .map(GroupRecommendationSummaryResult::from);
    }

    @Override
    public GroupRecommendationReadinessResult getGroupRecommendationReadiness(
            Long memberId,
            Long groupId,
            Long sessionId
    ) {
        Member member = memberReader.getActiveMember(memberId);
        GroupRoom room = groupRoomReader.getActiveGroupRoom(groupId);
        groupRoomReader.getActiveMembership(room.getId(), member.getId());

        GroupRecommendation recommendation = groupRecommendationRepository.findByIdAndRoomId(sessionId, room.getId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.RECOMMENDATION_NOT_FOUND, sessionId));
        groupRecommendationExpirationManager.expireGroupRecommendationIfNeeded(recommendation, LocalDateTime.now());

        List<GroupRoomMember> activeMembers = groupRoomMemberRepository.findActiveMembersByRoomId(room.getId());
        Map<Long, GroupRecommendationReadiness> readinessByMemberId = groupRecommendationReadinessRepository
                .findAllByGroupRecommendationId(recommendation.getId())
                .stream()
                .collect(Collectors.toMap(
                        readiness -> readiness.getMember().getId(),
                        Function.identity()
                ));

        GroupRecommendationReadinessProgressResult progress = groupRecommendationResultAssembler.readinessProgress(
                recommendation.getId(),
                room.getId(),
                activeMembers.size()
        );

        List<GroupRecommendationReadinessMemberResult> recommendationReadinessMemberResults = activeMembers.stream()
                .map(groupMember -> groupRecommendationResultAssembler.toReadinessMemberResult(
                        groupMember,
                        readinessByMemberId
                ))
                .toList();

        return new GroupRecommendationReadinessResult(
                recommendation.getId(),
                recommendation.getStatus(),
                progress,
                recommendationReadinessMemberResults
        );
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public ReadyGroupRecommendationResult readyGroupRecommendation(Long memberId, Long groupId, Long sessionId) {
        Member member = memberReader.getActiveMember(memberId);
        GroupRoom room = groupRoomReader.getActiveGroupRoom(groupId);
        groupRoomReader.getActiveMembership(room.getId(), member.getId());

        GroupRecommendation recommendation = groupRecommendationRepository.findByIdAndRoomId(sessionId, room.getId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.RECOMMENDATION_NOT_FOUND, sessionId));

        validateGroupRecommendationPreparing(recommendation, sessionId);

        groupRecommendationReadinessRepository
                .findByGroupRecommendationIdAndMemberId(recommendation.getId(), member.getId())
                .ifPresentOrElse(
                        GroupRecommendationReadiness::ready,
                        () -> groupRecommendationReadinessRepository.save(new GroupRecommendationReadiness(
                                recommendation,
                                member
                        ))
                );

        int totalMemberCount = groupRoomMemberRepository.findActiveMembersByRoomId(room.getId()).size();
        GroupRecommendationReadinessProgressResult readiness = groupRecommendationResultAssembler.readinessProgress(
                recommendation.getId(),
                room.getId(),
                totalMemberCount
        );

        List<GroupRecommendationCandidate> candidates = List.of();
        if (readiness.allReady()) {
            candidates = groupRecommendationCandidateGenerator.generateCandidatesForRecommendation(
                    room,
                    recommendation,
                    null,
                    groupRecommendationHistoryReader.recentlySkippedMenuIds(room.getId())
            );
            recommendation.open(LocalDateTime.now());
        }
        List<GroupRecommendationCandidateResult> candidateResults =
                groupRecommendationResultAssembler.toCandidateResults(candidates, 0);

        eventPublisher.publishEvent(new GroupRecommendationReadinessUpdatedRealtimeEvent(
                room.getId(),
                recommendation.getId(),
                member.getId(),
                member.getNickname(),
                recommendation.getStatus(),
                readiness
        ));

        if (readiness.allReady()) {
            eventPublisher.publishEvent(new GroupRecommendationOpenedRealtimeEvent(
                    room.getId(),
                    recommendation.getId(),
                    recommendation.getStatus(),
                    candidateResults,
                    groupRecommendationResultAssembler.toVoteProgress(recommendation)
            ));
        }

        return new ReadyGroupRecommendationResult(
                recommendation.getId(),
                recommendation.getStatus(),
                readiness,
                candidateResults
        );
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public GroupVoteResult voteGroupRecommendation(Long memberId, Long groupId, Long sessionId, Long candidateId) {
        Member member = memberReader.getActiveMember(memberId);
        groupRoomReader.getActiveMembership(groupId, member.getId());

        GroupRecommendation recommendation = groupRecommendationRepository.findByIdAndRoomId(sessionId, groupId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.RECOMMENDATION_NOT_FOUND, sessionId));

        validateGroupRecommendationOpen(recommendation, sessionId);

        GroupRecommendationCandidate candidate = groupRecommendationCandidateRepository
                .findByIdAndGroupRecommendationId(candidateId, recommendation.getId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.RECOMMENDATION_CANDIDATE_NOT_FOUND,
                        candidateId));

        GroupRecommendationVote vote = groupRecommendationVoteRepository
                .findByGroupRecommendationIdAndMemberId(recommendation.getId(), member.getId())
                .map(existingVote -> {
                    if (!existingVote.hasCandidate(candidate.getId())) {
                        existingVote.changeCandidate(candidate);
                    }

                    return existingVote;
                })
                .orElseGet(() -> new GroupRecommendationVote(
                        recommendation,
                        candidate,
                        member
        ));
        GroupRecommendationVote savedVote = groupRecommendationVoteRepository.saveAndFlush(vote);
        GroupVoteProgressResult voteProgress = groupRecommendationResultAssembler.toVoteProgress(recommendation);

        eventPublisher.publishEvent(new GroupRecommendationVoteUpdatedRealtimeEvent(
                groupId,
                recommendation.getId(),
                voteProgress
        ));

        if (voteProgress.totalMemberCount() > 0
                && voteProgress.totalMemberCount().equals(voteProgress.votedMemberCount())) {
            eventPublisher.publishEvent(new GroupRecommendationVoteCompletedRealtimeEvent(
                    groupId,
                    recommendation.getId(),
                    recommendation.getRoom().getHostMember().getId(),
                    voteProgress
            ));
        }

        return new GroupVoteResult(
                savedVote.getId(),
                candidate.getId(),
                savedVote.getUpdatedAt()
        );
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public FinalizeGroupRecommendationResult finalizeGroupRecommendation(
            Long memberId,
            FinalizeGroupRecommendationCommand command
    ) {
        Member member = memberReader.getActiveMember(memberId);
        GroupRoomMember membership = groupRoomReader.getActiveMembership(command.groupId(), member.getId());

        if (!membership.isOwner()) {
            throw new BusinessException(GroupErrorCode.RECOMMENDATION_FINALIZE_FORBIDDEN, command.groupId());
        }

        GroupRecommendation recommendation = groupRecommendationRepository
                .findByIdAndRoomId(command.sessionId(), command.groupId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.RECOMMENDATION_NOT_FOUND, command.sessionId()));

        validateGroupRecommendationOpen(recommendation, command.sessionId());

        List<GroupRecommendationCandidate> candidates = groupRecommendationCandidateRepository
                .findAllByGroupRecommendationIdOrderByRankNoAsc(recommendation.getId());

        if (candidates.isEmpty()) {
            throw new BusinessException(GroupErrorCode.RECOMMENDATION_NO_CANDIDATES, command.sessionId());
        }

        Map<Long, Integer> voteCountsByCandidateId =
                groupRecommendationResultAssembler.countVotesByCandidateId(recommendation.getId());
        GroupRecommendationCandidate selectedCandidate =
                groupFinalCandidateSelector.select(candidates, voteCountsByCandidateId);
        LocalDateTime finalizedAt = LocalDateTime.now();
        recommendation.finalizeWith(selectedCandidate, finalizedAt);
        recommendationLocationContextJsonFactory.createIfComplete(
                command.latitude(),
                command.longitude(),
                command.radiusMeters(),
                command.address()
        ).ifPresent(recommendation::saveContextJson);
        GroupRecommendationCandidateResult finalCandidate = GroupRecommendationCandidateResult.from(
                selectedCandidate,
                voteCountsByCandidateId.getOrDefault(selectedCandidate.getId(), 0)
        );

        eventPublisher.publishEvent(new GroupRecommendationFinalizedRealtimeEvent(
                command.groupId(),
                recommendation.getId(),
                member.getId(),
                recommendation.getStatus(),
                finalCandidate,
                finalizedAt
        ));

        return new FinalizeGroupRecommendationResult(
                recommendation.getId(),
                recommendation.getStatus(),
                finalCandidate,
                finalizedAt
        );
    }

    @Override
    public List<GroupHomeActivityResult> getHomeActivities(Long memberId) {
        Member member = memberReader.getActiveMember(memberId);
        var recommendations = groupRecommendationRepository.findHistoryForActiveMember(member.getId());
        LocalDateTime now = LocalDateTime.now();
        recommendations.forEach(recommendation ->
                groupRecommendationExpirationManager.expireGroupRecommendationIfNeeded(recommendation, now)
        );
        return recommendations.stream()
                .map(GroupHomeActivityResult::from)
                .sorted(Comparator.comparing(GroupHomeActivityResult::activityAt).reversed()
                        .thenComparing(GroupHomeActivityResult::recommendationId, Comparator.reverseOrder()))
                .toList();
    }

    private void validateGroupRecommendationPreparing(GroupRecommendation recommendation, Long sessionId) {
        validateGroupRecommendationNotExpired(recommendation, sessionId);

        if (recommendation.getStatus() != GroupRecommendationStatus.PREPARING) {
            throw new BusinessException(GroupErrorCode.RECOMMENDATION_NOT_PREPARING, sessionId);
        }
    }

    private void validateGroupRecommendationOpen(GroupRecommendation recommendation, Long sessionId) {
        validateGroupRecommendationNotExpired(recommendation, sessionId);

        if (recommendation.getStatus() != GroupRecommendationStatus.OPEN) {
            throw new BusinessException(GroupErrorCode.RECOMMENDATION_NOT_OPEN, sessionId);
        }
    }

    private void validateGroupRecommendationNotExpired(GroupRecommendation recommendation, Long sessionId) {
        if (groupRecommendationExpirationManager.expireGroupRecommendationIfNeeded(
                recommendation,
                LocalDateTime.now()
        )) {
            throw new BusinessException(GroupErrorCode.RECOMMENDATION_EXPIRED, sessionId);
        }
    }
}



