package matchuri.backend.domain.group.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.group.command.*;
import matchuri.backend.domain.group.entity.*;
import matchuri.backend.domain.group.exception.GroupErrorCode;
import matchuri.backend.domain.group.repository.*;
import matchuri.backend.domain.group.result.*;
import matchuri.backend.domain.group.support.GroupInviteCodeGenerator;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.support.member.ActiveMemberReader;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.entity.MenuAttributeCategory;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.repository.MenuIngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import matchuri.backend.domain.menu.support.MenuThumbnailUrlResolver;
import matchuri.backend.domain.recommendation.algorithm.MenuRecommendationAlgorithm;
import matchuri.backend.domain.recommendation.algorithm.MenuRecommendationAlgorithmResolver;
import matchuri.backend.domain.recommendation.algorithm.RecommendationAlgorithmType;
import matchuri.backend.domain.recommendation.algorithm.RecommendationTargetType;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationInput;
import matchuri.backend.domain.recommendation.algorithm.input.MenuRecommendationProfile;
import matchuri.backend.domain.recommendation.algorithm.input.RecommendationContextSnapshot;
import matchuri.backend.domain.recommendation.algorithm.input.TasteProfileSnapshot;
import matchuri.backend.domain.recommendation.algorithm.output.MenuRecommendationCandidateResult;
import matchuri.backend.domain.recommendation.algorithm.output.MenuRecommendationResult;
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
public class GroupServiceImpl implements GroupService {

    private static final int MAX_INVITE_CODE_GENERATION_ATTEMPTS = 5;
    private static final int NICKNAME_INVITE_EXPIRATION_HOURS = 24;
    private static final int GROUP_RECOMMENDATION_CANDIDATE_LIMIT = 3;
    private static final long RECENT_GROUP_SKIPPED_MENU_EXCLUSION_HOURS = 24;
    private static final List<GroupRecommendationStatus> ACTIVE_RECOMMENDATION_STATUSES = List.of(
            GroupRecommendationStatus.PREPARING,
            GroupRecommendationStatus.OPEN
    );

    private final ActiveMemberReader activeMemberReader;
    private final MemberRepository memberRepository;
    private final GroupRoomRepository groupRoomRepository;
    private final GroupLocationRepository groupLocationRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupInviteRepository groupInviteRepository;
    private final GroupMenuActionRepository groupMenuActionRepository;
    private final GroupRecommendationRepository groupRecommendationRepository;
    private final GroupRecommendationCandidateRepository groupRecommendationCandidateRepository;
    private final GroupRecommendationReadinessRepository groupRecommendationReadinessRepository;
    private final GroupRecommendationVoteRepository groupRecommendationVoteRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuIngredientRepository menuIngredientRepository;
    private final MenuThumbnailUrlResolver menuThumbnailUrlResolver;
    private final MenuRecommendationAlgorithmResolver menuRecommendationAlgorithmResolver;
    private final GroupInviteCodeGenerator groupInviteCodeGenerator;
    private final GroupRecommendationExpirationService groupRecommendationExpirationService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CreateGroupResult createGroup(CreateGroupCommand command) {
        Member hostMember = activeMemberReader.getCurrentAuthenticatedActiveMember();
        String inviteCode = createUniqueInviteCode();

        GroupRoom groupRoom = GroupRoom.createOwnedBy(
                command.name(),
                inviteCode,
                hostMember);

        GroupRoom savedGroupRoom = groupRoomRepository.save(groupRoom);
        updateLatestGroupLocation(
                savedGroupRoom,
                command.latitude(),
                command.longitude(),
                command.radiusMeters(),
                command.address()
        );

        return new CreateGroupResult(
                savedGroupRoom.getId(),
                savedGroupRoom.getInviteCode(),
                savedGroupRoom.getStatus()
        );
    }

    @Override
    public CreateGroupRecommendationResult createGroupRecommendation(CreateGroupRecommendationCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = getActiveGroupRoom(command.groupId());

        if (!validateActiveMembership(room.getId(), member.getId()).isOwner()) {
            throw new BusinessException(GroupErrorCode.RECOMMENDATION_CREATE_FORBIDDEN, room.getId());
        }

        if (hasActiveRecommendation(room.getId())) {
            throw new BusinessException(GroupErrorCode.RECOMMENDATION_ACTIVE_EXISTS, room.getId());
        }

        updateLatestGroupLocation(
                room,
                command.latitude(),
                command.longitude(),
                command.radiusMeters(),
                command.address()
        );

        GroupRecommendation recommendation = groupRecommendationRepository.save(
                GroupRecommendation.preparing(room, LocalDateTime.now())
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
            Long groupId,
            Long sessionId,
            GroupRecommendationRerollType rerollType,
            String contextJson
    ) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = getActiveGroupRoom(groupId);
        GroupRoomMember membership = validateActiveMembership(room.getId(), member.getId());

        if (!membership.isOwner()) {
            throw new BusinessException(GroupErrorCode.RECOMMENDATION_REROLL_FORBIDDEN, room.getId());
        }

        GroupRecommendation sourceRecommendation = groupRecommendationRepository.findByIdAndRoomId(sessionId, groupId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.RECOMMENDATION_NOT_FOUND, sessionId));

        validateGroupRecommendationOpen(sourceRecommendation, sessionId);

        LocalDateTime endedAt = LocalDateTime.now();

        if (rerollType == GroupRecommendationRerollType.NOT_SATISFIED) {
            saveGroupSkipActions(room, sourceRecommendation, member);
            sourceRecommendation.rerollWithSkip(endedAt);
        } else if (rerollType == GroupRecommendationRerollType.INPUT_CHANGED) {
            sourceRecommendation.rerollWithoutSkip(endedAt);
        } else {
            throw new IllegalArgumentException("지원하지 않는 그룹 추천 재요청 타입입니다. rerollType=" + rerollType);
        }

        GroupRecommendation newRecommendation = groupRecommendationRepository.save(new GroupRecommendation(
                room,
                contextJson,
                LocalDateTime.now()
        ));
        updateRoomLocationFromContextJson(room, contextJson);
        List<GroupRecommendationCandidate> candidates = generateCandidatesForRecommendation(
                room,
                newRecommendation,
                contextJson,
                recentlySkippedMenuIds(room.getId())
        );

        return new CreateGroupRecommendationResult(
                newRecommendation.getId(),
                newRecommendation.getStatus(),
                toCandidateResults(candidates, 0)
        );
    }

    private boolean hasActiveRecommendation(Long roomId) {
        return groupRecommendationRepository.existsByRoomIdAndStatusInAndStartedAtAfter(
                roomId,
                ACTIVE_RECOMMENDATION_STATUSES,
                groupRecommendationExpirationService.activeThreshold(LocalDateTime.now())
        );
    }

    private List<GroupRecommendationCandidate> generateCandidatesForRecommendation(
            GroupRoom room,
            GroupRecommendation recommendation,
            String contextJson,
            List<Long> excludedMenuIds
    ) {
        List<GroupRoomMember> activeMembers = groupRoomMemberRepository.findActiveMembersByRoomId(room.getId());
        List<MenuItem> menuItems = menuItemRepository.searchActiveMenuItems(null, List.of(), true, List.of(), true);
        Map<Long, MenuItem> menuItemById = menuItems.stream()
                .collect(Collectors.toMap(MenuItem::getId, Function.identity()));
        String recommendationContextJson = contextJson == null
                ? toRecommendationContextJson(room)
                : contextJson;

        MenuRecommendationAlgorithm algorithm =
                menuRecommendationAlgorithmResolver.resolve(RecommendationAlgorithmType.GROUP);

        MenuRecommendationResult recommendationResult = algorithm.recommend(new MenuRecommendationInput(
                RecommendationTargetType.GROUP,
                toTasteProfileSnapshots(activeMembers),
                toMenuRecommendationProfiles(menuItems),
                RecommendationContextSnapshot.of(recommendationContextJson),
                GROUP_RECOMMENDATION_CANDIDATE_LIMIT,
                List.of(),
                excludedMenuIds,
                Map.of()
        ));

        List<GroupRecommendationCandidate> candidates = saveGroupRecommendationCandidates(
                recommendation,
                recommendationResult,
                menuItemById
        );
        recommendation.openWithContextJson(recommendationContextJson);

        return candidates;
    }

    private GroupRoom getActiveGroupRoom(Long groupId) {
        GroupRoom room = groupRoomRepository.findByIdAndStatusNot(groupId, GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, groupId));

        if (!room.isActive()) {
            throw new BusinessException(GroupErrorCode.NOT_ACTIVE, room.getId());
        }

        return room;
    }

    private void saveGroupSkipActions(
            GroupRoom room,
            GroupRecommendation sourceRecommendation,
            Member actorMember
    ) {
        List<GroupMenuAction> skipActions = groupRecommendationCandidateRepository
                .findAllByGroupRecommendationIdOrderByRankNoAsc(sourceRecommendation.getId())
                .stream()
                .map(candidate -> new GroupMenuAction(
                        room,
                        sourceRecommendation,
                        actorMember,
                        candidate.getMenuItem(),
                        GroupMenuActionType.SKIP
                ))
                .toList();

        groupMenuActionRepository.saveAll(skipActions);
    }

    private List<Long> recentlySkippedMenuIds(Long groupRoomId) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(RECENT_GROUP_SKIPPED_MENU_EXCLUSION_HOURS);

        return groupMenuActionRepository.findMenuItemIdsByGroupRoomIdAndActionTypeAndCreatedAtAfter(
                groupRoomId,
                GroupMenuActionType.SKIP,
                threshold
        );
    }

    @Override
    @Transactional(readOnly = true)
    public GroupRecommendationResult getGroupRecommendation(Long groupId, Long sessionId) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        validateActiveMembership(groupId, member.getId());

        GroupRecommendation recommendation = groupRecommendationRepository.findByIdAndRoomId(sessionId, groupId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.RECOMMENDATION_NOT_FOUND, sessionId));

        return toGroupRecommendationResult(recommendation, member.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public GroupRecommendationCandidateListResult getGroupRecommendationCandidates(Long groupId, Long sessionId) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        validateActiveMembership(groupId, member.getId());

        GroupRecommendation recommendation = groupRecommendationRepository.findByIdAndRoomId(sessionId, groupId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.RECOMMENDATION_NOT_FOUND, sessionId));

        validateGroupRecommendationOpen(recommendation, sessionId);

        return new GroupRecommendationCandidateListResult(
                recommendation.getId(),
                toCandidateResults(recommendation)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull GroupRecommendationSummaryResult> getGroupRecommendations(Long groupId, int page, int size) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = getActiveGroupRoom(groupId);
        validateActiveMembership(room.getId(), member.getId());

        return groupRecommendationRepository
                .findVisibleByRoomIdOrderByStartedAtDescIdDesc(
                        room.getId(),
                        ACTIVE_RECOMMENDATION_STATUSES,
                        groupRecommendationExpirationService.activeThreshold(LocalDateTime.now()),
                        PageRequest.of(page, size)
                )
                .map(GroupRecommendationSummaryResult::from);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupRecommendationReadinessResult getGroupRecommendationReadiness(Long groupId, Long sessionId) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = getActiveGroupRoom(groupId);
        validateActiveMembership(room.getId(), member.getId());

        GroupRecommendation recommendation = groupRecommendationRepository.findByIdAndRoomId(sessionId, room.getId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.RECOMMENDATION_NOT_FOUND, sessionId));

        List<GroupRoomMember> activeMembers = groupRoomMemberRepository.findActiveMembersByRoomId(room.getId());
        Map<Long, GroupRecommendationReadiness> readinessByMemberId = groupRecommendationReadinessRepository
                .findAllByGroupRecommendationId(recommendation.getId())
                .stream()
                .collect(Collectors.toMap(
                        readiness -> readiness.getMember().getId(),
                        Function.identity()
                ));

        GroupRecommendationReadinessProgressResult progress = readinessProgress(
                recommendation.getId(),
                room.getId(),
                activeMembers.size()
        );

        List<GroupRecommendationReadinessMemberResult> recommendationReadinessMemberResults = activeMembers.stream()
                .map(groupMember -> toReadinessMemberResult(groupMember, readinessByMemberId))
                .toList();

        return new GroupRecommendationReadinessResult(
                recommendation.getId(),
                recommendation.getStatus(),
                progress,
                recommendationReadinessMemberResults
        );
    }

    @Override
    public ReadyGroupRecommendationResult readyGroupRecommendation(Long groupId, Long sessionId) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = getActiveGroupRoom(groupId);
        validateActiveMembership(room.getId(), member.getId());

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
        GroupRecommendationReadinessProgressResult readiness = readinessProgress(
                recommendation.getId(),
                room.getId(),
                totalMemberCount
        );

        List<GroupRecommendationCandidate> candidates = List.of();
        if (readiness.allReady()) {
            candidates = generateCandidatesForRecommendation(
                    room,
                    recommendation,
                    null,
                    recentlySkippedMenuIds(room.getId())
            );
        }
        List<GroupRecommendationCandidateResult> candidateResults = toCandidateResults(candidates, 0);

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
                    toVoteProgress(recommendation)
            ));
        }

        return new ReadyGroupRecommendationResult(
                recommendation.getId(),
                recommendation.getStatus(),
                readiness,
                candidateResults
        );
    }

    private GroupRecommendationReadinessProgressResult readinessProgress(
            Long recommendationId,
            Long roomId,
            int totalMemberCount
    ) {
        int readyMemberCount = Math.toIntExact(groupRecommendationReadinessRepository
                .countActiveMemberReadinessByRecommendationIdAndStatus(
                        recommendationId,
                        roomId,
                        GroupRecommendationReadinessStatus.READY
                ));

        return GroupRecommendationReadinessProgressResult.of(totalMemberCount, readyMemberCount);
    }

    private GroupRecommendationReadinessMemberResult toReadinessMemberResult(
            GroupRoomMember groupMember,
            Map<Long, GroupRecommendationReadiness> readinessByMemberId
    ) {
        Member member = groupMember.getMember();
        GroupRecommendationReadiness readiness = readinessByMemberId.get(member.getId());
        GroupRecommendationReadinessStatus readinessStatus = readiness == null ? null : readiness.getStatus();

        return new GroupRecommendationReadinessMemberResult(
                member.getId(),
                member.getNickname(),
                groupMember.getRole(),
                readinessStatus == GroupRecommendationReadinessStatus.READY
        );
    }

    @Override
    @Transactional
    public GroupVoteResult voteGroupRecommendation(Long groupId, Long sessionId, Long candidateId) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        validateActiveMembership(groupId, member.getId());

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
        GroupVoteProgressResult voteProgress = toVoteProgress(recommendation);

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
    public FinalizeGroupRecommendationResult finalizeGroupRecommendation(Long groupId, Long sessionId) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoomMember membership = validateActiveMembership(groupId, member.getId());

        if (!membership.isOwner()) {
            throw new BusinessException(GroupErrorCode.RECOMMENDATION_FINALIZE_FORBIDDEN, groupId);
        }

        GroupRecommendation recommendation = groupRecommendationRepository.findByIdAndRoomId(sessionId, groupId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.RECOMMENDATION_NOT_FOUND, sessionId));

        validateGroupRecommendationOpen(recommendation, sessionId);

        List<GroupRecommendationCandidate> candidates = groupRecommendationCandidateRepository
                .findAllByGroupRecommendationIdOrderByRankNoAsc(recommendation.getId());

        if (candidates.isEmpty()) {
            throw new BusinessException(GroupErrorCode.RECOMMENDATION_NO_CANDIDATES, sessionId);
        }

        Map<Long, Integer> voteCountsByCandidateId = countVotesByCandidateId(recommendation.getId());
        GroupRecommendationCandidate selectedCandidate = selectFinalCandidate(candidates, voteCountsByCandidateId);
        LocalDateTime finalizedAt = LocalDateTime.now();
        recommendation.finalizeWith(selectedCandidate, finalizedAt);
        GroupRecommendationCandidateResult finalCandidate = GroupRecommendationCandidateResult.from(
                selectedCandidate,
                voteCountsByCandidateId.getOrDefault(selectedCandidate.getId(), 0)
        );

        eventPublisher.publishEvent(new GroupRecommendationFinalizedRealtimeEvent(
                groupId,
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
    public CreateNicknameGroupInviteResult createNicknameInvite(CreateNicknameGroupInviteCommand command) {
        Member requestMember = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = groupRoomRepository.findByIdAndStatusNot(command.groupId(), GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, command.groupId()));

        if (!room.isActive()) {
            throw new BusinessException(GroupErrorCode.NOT_ACTIVE, command.groupId());
        }

        GroupRoomMember requestMembership = groupRoomMemberRepository
                .findActiveMembershipInNotDeletedRoom(room.getId(), requestMember.getId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.INVITE_FORBIDDEN, room.getId()));

        if (!requestMembership.isOwner()) {
            throw new BusinessException(GroupErrorCode.INVITE_FORBIDDEN, room.getId());
        }

        Member targetMember = memberRepository.findByNicknameAndStatus(command.nickname(), MemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.INVITE_TARGET_NOT_FOUND, command.nickname()));

        if (requestMember.getId().equals(targetMember.getId())) {
            throw new BusinessException(GroupErrorCode.INVITE_SELF_NOT_ALLOWED, requestMember.getId());
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

    @Override
    public JoinGroupResult joinGroup(JoinGroupCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = groupRoomRepository.findByInviteCode(command.inviteCode())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.INVITE_NOT_FOUND, command.inviteCode()));

        if (!room.isActive()) {
            throw new BusinessException(GroupErrorCode.NOT_ACTIVE, room.getId());
        }

        GroupRoomMember membership = joinOrRejoinMember(room, member);

        eventPublisher.publishEvent(new GroupMemberJoinedRealtimeEvent(
                room.getId(),
                member.getId(),
                member.getNickname(),
                membership.getJoinedAt()
        ));

        return new JoinGroupResult(room.getId(), membership.getStatus());
    }

    @Override
    public LeaveGroupResult leaveGroup(LeaveGroupCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        Long memberId = member.getId();
        GroupRoom room = groupRoomRepository.findByIdAndStatusNot(command.groupId(), GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, command.groupId()));
        GroupRoomMember membership = room.getGroupRoomMemberById(memberId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.MEMBER_NOT_FOUND, room.getId(), memberId));

        if (membership.isLeft()) {
            throw new BusinessException(GroupErrorCode.MEMBER_ALREADY_LEFT, room.getId(), memberId);
        }

        if (!membership.isActive()) {
            throw new BusinessException(GroupErrorCode.MEMBER_NOT_FOUND, room.getId(), memberId);
        }

        if (membership.isOwner()) {
            throw new BusinessException(GroupErrorCode.OWNER_LEAVE_NOT_ALLOWED, room.getId());
        }

        LocalDateTime leftAt = LocalDateTime.now();
        membership.leave(leftAt);

        eventPublisher.publishEvent(new GroupMemberLeftRealtimeEvent(
                room.getId(),
                member.getId(),
                member.getNickname(),
                membership.getLeftAt()
        ));

        return new LeaveGroupResult(room.getId(), membership.getStatus(), membership.getLeftAt());
    }

    @Override
    @Transactional
    public DeleteGroupResult deleteGroup(DeleteGroupCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = groupRoomRepository.findByIdAndStatusNot(command.groupId(), GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, command.groupId()));
        GroupRoomMember membership = groupRoomMemberRepository
                .findActiveMembershipInNotDeletedRoom(room.getId(), member.getId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.ACCESS_DENIED, room.getId()));

        if (!membership.isOwner()) {
            throw new BusinessException(GroupErrorCode.DELETE_FORBIDDEN, room.getId());
        }

        LocalDateTime deletedAt = LocalDateTime.now();
        List<Long> targetMemberIds = groupRoomMemberRepository.findActiveMembersByRoomId(room.getId()).stream()
                .map(GroupRoomMember::getMember)
                .map(Member::getId)
                .toList();

        room.delete();
        revokeActiveInvites(room);
        leaveActiveMembers(room, deletedAt);

        eventPublisher.publishEvent(new GroupDeletedRealtimeEvent(
                room.getId(),
                member.getId(),
                targetMemberIds,
                deletedAt
        ));

        return new DeleteGroupResult(room.getId(), room.getStatus(), deletedAt);
    }

    @Override
    public UpdateGroupResult updateGroup(UpdateGroupCommand command) {
        if (command.hasNoFields()) {
            throw new BusinessException(GroupErrorCode.UPDATE_EMPTY_REQUEST);
        }

        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = groupRoomRepository.findByIdAndStatusNot(command.groupId(), GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, command.groupId()));

        if (!room.isActive()) {
            throw new BusinessException(GroupErrorCode.NOT_ACTIVE, room.getId());
        }

        GroupRoomMember membership = room.getGroupRoomMemberById(member.getId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.ACCESS_DENIED, room.getId()));

        if (!membership.isOwner()) {
            throw new BusinessException(GroupErrorCode.UPDATE_FORBIDDEN, room.getId());
        }

        if (command.name() != null) {
            room.updateName(command.name());
        }

        GroupLocation location = updateLatestGroupLocation(
                room,
                command.latitude(),
                command.longitude(),
                command.radiusMeters(),
                command.address()
        );

        return new UpdateGroupResult(
                room.getId(),
                room.getName(),
                location == null ? null : location.getLatitude(),
                location == null ? null : location.getLongitude(),
                location == null ? null : location.getRadiusMeters(),
                location == null ? null : location.getAddress(),
                room.getStatus(),
                room.getUpdatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull GroupSummaryResult> getMyGroups(GetMyGroupsCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        Page<@NonNull GroupRoomMember> memberships = groupRoomMemberRepository.findMyActiveMemberships(
                member.getId(),
                command.status(),
                PageRequest.of(command.page(), command.size())
        );
        Map<Long, Long> activeMemberCounts = countActiveMembers(memberships);

        return memberships.map(membership -> toSummaryResult(membership, activeMemberCounts));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull GroupInviteSummaryResult> getMyInvites(GetMyGroupInvitesCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupInviteStatus status = command.status() == null ? GroupInviteStatus.PENDING : command.status();

        return groupInviteRepository.findMyInvites(
                member.getId(),
                status,
                LocalDateTime.now(),
                PageRequest.of(command.page(), command.size())).map(this::toInviteSummaryResult);
    }

    @Override
    public RespondGroupInviteResult respondGroupInvite(RespondGroupInviteCommand command) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupInvite invite = groupInviteRepository.findById(command.inviteId())
                .orElseThrow(() -> new BusinessException(GroupErrorCode.INVITE_REQUEST_NOT_FOUND, command.inviteId()));

        if (!invite.getTargetMember().getId().equals(member.getId())) {
            throw new BusinessException(GroupErrorCode.INVITE_RESPONSE_FORBIDDEN, invite.getId());
        }

        LocalDateTime now = LocalDateTime.now();
        validateRespondableInvite(invite, now);

        GroupRoom room = invite.getRoom();
        GroupMemberStatus memberStatus = null;

        if (!room.isActive()) {
            throw new BusinessException(GroupErrorCode.NOT_ACTIVE, room.getId());
        }

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

    @Override
    @Transactional(readOnly = true)
    public GroupDetailResult getGroup(Long groupId) {
        Member member = activeMemberReader.getCurrentAuthenticatedActiveMember();
        GroupRoom room = groupRoomRepository.findByIdAndStatusNot(groupId, GroupRoomStatus.DELETED)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.NOT_FOUND, groupId));

        if (!groupRoomMemberRepository.existsActiveMembershipInNotDeletedRoom(groupId, member.getId())) {
            throw new BusinessException(GroupErrorCode.ACCESS_DENIED, groupId);
        }

        List<GroupMemberSummaryResult> members = groupRoomMemberRepository.findActiveMembersByRoomId(groupId)
                .stream()
                .map(membership -> toMemberSummaryResult(membership, member.getId()))
                .toList();
        GroupRecommendationResult recentlyRecommendation = groupRecommendationRepository
                .findFirstByRoomIdOrderByStartedAtDescIdDesc(groupId)
                .map(recommendation -> toGroupRecommendationResult(recommendation, member.getId()))
                .orElse(null);
        GroupLocation location = latestGroupLocation(room.getId());

        return new GroupDetailResult(
                room.getId(),
                room.getName(),
                room.getInviteCode(),
                location == null ? null : location.getLatitude(),
                location == null ? null : location.getLongitude(),
                location == null ? null : location.getRadiusMeters(),
                location == null ? null : location.getAddress(),
                room.getStatus(),
                members,
                recentlyRecommendation
        );
    }

    private GroupRoomMember validateActiveMembership(Long groupId, Long memberId) {
        return groupRoomMemberRepository.findActiveMembershipInNotDeletedRoom(groupId, memberId)
                .orElseThrow(() -> new BusinessException(GroupErrorCode.ACCESS_DENIED, groupId));
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
        if (recommendation.getStatus() == GroupRecommendationStatus.EXPIRED
                || groupRecommendationExpirationService.isExpired(recommendation, LocalDateTime.now())) {
            throw new BusinessException(GroupErrorCode.RECOMMENDATION_EXPIRED, sessionId);
        }
    }

    private GroupRecommendationResult toGroupRecommendationResult(
            GroupRecommendation recommendation,
            Long currentMemberId
    ) {
        boolean preparing = recommendation.getStatus() == GroupRecommendationStatus.PREPARING;
        List<GroupRecommendationCandidateResult> candidates = preparing ? List.of() : toCandidateResults(recommendation);
        GroupRecommendationCandidateResult finalCandidate = recommendation.getSelectedCandidate() == null
                ? null
                : candidates.stream()
                        .filter(candidate -> candidate.candidateId().equals(
                                recommendation.getSelectedCandidate().getId()))
                        .findFirst()
                        .orElseGet(() -> GroupRecommendationCandidateResult.from(
                                recommendation.getSelectedCandidate(),
                                0,
                                menuThumbnailUrlResolver.resolve(
                                        recommendation.getSelectedCandidate().getMenuItem().getId())
                        ));

        return new GroupRecommendationResult(
                recommendation.getId(),
                recommendation.getStatus(),
                responseContextJson(recommendation.getContextJson()),
                preparing
                        ? readinessProgress(
                                recommendation.getId(),
                                recommendation.getRoom().getId(),
                                groupRoomMemberRepository.findActiveMembersByRoomId(recommendation.getRoom().getId())
                                        .size()
                        )
                        : null,
                candidates,
                preparing ? null : toVoteProgress(recommendation),
                preparing ? List.of() : toMemberVoteResults(recommendation, currentMemberId),
                finalCandidate,
                recommendation.getCreatedAt()
        );
    }

    private List<GroupMemberVoteResult> toMemberVoteResults(
            GroupRecommendation recommendation,
            Long currentMemberId
    ) {
        Map<Long, GroupRecommendationVote> votesByMemberId = groupRecommendationVoteRepository
                .findAllByGroupRecommendationId(recommendation.getId())
                .stream()
                .collect(Collectors.toMap(
                        vote -> vote.getMember().getId(),
                        Function.identity()
                ));

        return groupRoomMemberRepository.findActiveMembersByRoomId(recommendation.getRoom().getId())
                .stream()
                .map(membership -> {
                    Long memberId = membership.getMember().getId();
                    GroupRecommendationVote vote = votesByMemberId.get(memberId);

                    return new GroupMemberVoteResult(
                            memberId,
                            membership.getMember().getNickname(),
                            membership.getRole(),
                            memberId.equals(currentMemberId),
                            vote != null,
                            vote == null ? null : vote.getCandidate().getId()
                    );
                })
                .toList();
    }

    private String responseContextJson(String contextJson) {
        if (contextJson == null || contextJson.isBlank()) {
            return contextJson;
        }

        String normalized = contextJson;
        while (normalized.startsWith("\"") && normalized.endsWith("\"")) {
            try {
                JsonNode contextNode = objectMapper.readTree(normalized);
                if (!contextNode.isTextual()) {
                    return normalized;
                }
                normalized = contextNode.asText();
            } catch (JsonProcessingException exception) {
                return contextJson;
            }
        }

        return normalized;
    }

    private List<GroupRecommendationCandidateResult> toCandidateResults(GroupRecommendation recommendation) {
        Map<Long, Integer> voteCountsByCandidateId = countVotesByCandidateId(recommendation.getId());
        List<GroupRecommendationCandidate> candidates = groupRecommendationCandidateRepository
                .findAllByGroupRecommendationIdOrderByRankNoAsc(recommendation.getId());
        Map<Long, String> thumbnailUrlsByMenuId = thumbnailUrlsByMenuId(candidates);

        return candidates.stream()
                .map(candidate -> GroupRecommendationCandidateResult.from(
                        candidate,
                        voteCountsByCandidateId.getOrDefault(candidate.getId(), 0),
                        thumbnailUrlsByMenuId.get(candidate.getMenuItem().getId())
                ))
                .toList();
    }

    private List<GroupRecommendationCandidateResult> toCandidateResults(
            List<GroupRecommendationCandidate> candidates,
            int voteCount
    ) {
        Map<Long, String> thumbnailUrlsByMenuId = thumbnailUrlsByMenuId(candidates);

        return candidates.stream()
                .map(candidate -> GroupRecommendationCandidateResult.from(
                        candidate,
                        voteCount,
                        thumbnailUrlsByMenuId.get(candidate.getMenuItem().getId())
                ))
                .toList();
    }

    private Map<Long, String> thumbnailUrlsByMenuId(List<GroupRecommendationCandidate> candidates) {
        return menuThumbnailUrlResolver.resolveAll(candidates.stream()
                .map(GroupRecommendationCandidate::getMenuItem)
                .map(MenuItem::getId)
                .toList());
    }

    private Map<Long, Integer> countVotesByCandidateId(Long recommendationId) {
        return groupRecommendationVoteRepository.countVotesByCandidateId(recommendationId)
                .stream()
                .collect(Collectors.toMap(
                        GroupCandidateVoteCountProjection::getCandidateId,
                        projection -> projection.getVoteCount().intValue()
                ));
    }

    private GroupRecommendationCandidate selectFinalCandidate(
            List<GroupRecommendationCandidate> candidates,
            Map<Long, Integer> voteCountsByCandidateId
    ) {
        return candidates.stream()
                .max((left, right) -> {
                    int voteComparison = Integer.compare(
                            voteCountsByCandidateId.getOrDefault(left.getId(), 0),
                            voteCountsByCandidateId.getOrDefault(right.getId(), 0)
                    );

                    if (voteComparison != 0) {
                        return voteComparison;
                    }

                    return Integer.compare(right.getRankNo(), left.getRankNo());
                })
                .orElseThrow();
    }

    private GroupVoteProgressResult toVoteProgress(GroupRecommendation recommendation) {
        int totalMemberCount = groupRoomMemberRepository.findActiveMembersByRoomId(recommendation.getRoom().getId())
                .size();
        int votedMemberCount = Math.toIntExact(
                groupRecommendationVoteRepository.countByGroupRecommendationId(recommendation.getId())
        );

        return new GroupVoteProgressResult(totalMemberCount, votedMemberCount);
    }

    private List<TasteProfileSnapshot> toTasteProfileSnapshots(List<GroupRoomMember> activeMembers) {
        return activeMembers.stream()
                .map(GroupRoomMember::getMember)
                .map(member -> toTasteProfileSnapshot(member, member.getTasteProfile()))
                .toList();
    }

    private TasteProfileSnapshot toTasteProfileSnapshot(Member member, MemberTasteProfile tasteProfile) {
        if (tasteProfile == null) {
            return new TasteProfileSnapshot(
                    member.getId(),
                    String.valueOf(member.getId()),
                    List.of(),
                    List.of(),
                    List.of()
            );
        }

        return new TasteProfileSnapshot(
                member.getId(),
                String.valueOf(member.getId()),
                tasteProfile.getPreferAttributeCategories().stream()
                        .map(AttributeCategory::getId)
                        .toList(),
                tasteProfile.getRestrictionIngredients().stream()
                        .map(Ingredient::getId)
                        .toList(),
                tasteProfile.getDisLikeMenuItems().stream()
                        .map(MenuItem::getId)
                        .toList()
        );
    }

    private List<MenuRecommendationProfile> toMenuRecommendationProfiles(List<MenuItem> menuItems) {
        Map<Long, List<Long>> ingredientIdsByMenuId = menuIngredientRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        menuIngredient -> menuIngredient.getMenu().getId(),
                        Collectors.mapping(menuIngredient -> menuIngredient.getIngredient().getId(),
                                Collectors.toList())
                ));

        return menuItems.stream()
                .map(menuItem -> new MenuRecommendationProfile(
                        menuItem.getId(),
                        menuItem.getCode(),
                        menuItem.getName(),
                        menuItem.getMenuAttributeCategories().stream()
                                .map(MenuAttributeCategory::getAttributeCategory)
                                .map(AttributeCategory::getId)
                                .toList(),
                        ingredientIdsByMenuId.getOrDefault(menuItem.getId(), List.of())
                ))
                .toList();
    }

    private List<GroupRecommendationCandidate> saveGroupRecommendationCandidates(
            GroupRecommendation recommendation,
            MenuRecommendationResult recommendationResult,
            Map<Long, MenuItem> menuItemById
    ) {
        List<GroupRecommendationCandidate> candidates = recommendationResult.candidates().stream()
                .map(candidate -> new GroupRecommendationCandidate(
                        recommendation,
                        menuItemById.get(candidate.menuId()),
                        candidate.rankNo(),
                        candidate.score(),
                        toCandidateMetaJson(recommendationResult, candidate)
                ))
                .toList();

        return groupRecommendationCandidateRepository.saveAll(candidates);
    }

    private String toCandidateMetaJson(
            MenuRecommendationResult recommendationResult,
            MenuRecommendationCandidateResult candidate
    ) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("algorithmType", recommendationResult.algorithmType().name());
        meta.put("algorithmVersion", recommendationResult.algorithmVersion());
        meta.put("scoreBreakdown", candidate.scoreBreakdown());
        meta.put("candidateMeta", candidate.meta());

        try {
            return objectMapper.writeValueAsString(meta);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("그룹 추천 후보 메타 정보를 JSON으로 변환할 수 없습니다.", exception);
        }
    }

    private Map<Long, Long> countActiveMembers(Page<@NonNull GroupRoomMember> memberships) {
        List<Long> roomIds = memberships.getContent().stream()
                .map(membership -> membership.getRoom().getId())
                .toList();

        if (roomIds.isEmpty()) {
            return Map.of();
        }

        return groupRoomMemberRepository.countMembersByRoomIdsAndStatus(roomIds, GroupMemberStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(
                        GroupRoomMemberCountProjection::getRoomId,
                        GroupRoomMemberCountProjection::getMemberCount
                ));
    }

    private GroupSummaryResult toSummaryResult(
            GroupRoomMember membership,
            Map<Long, Long> activeMemberCounts
    ) {
        GroupRoom room = membership.getRoom();

        return new GroupSummaryResult(
                room.getId(),
                room.getName(),
                room.getStatus(),
                activeMemberCounts.getOrDefault(room.getId(), 0L).intValue(),
                latestVisibleRecommendationStatus(room.getId()),
                room.getCreatedAt()
        );
    }

    private GroupRecommendationStatus latestVisibleRecommendationStatus(Long roomId) {
        return groupRecommendationRepository.findVisibleByRoomIdOrderByStartedAtDescIdDesc(
                        roomId,
                        ACTIVE_RECOMMENDATION_STATUSES,
                        groupRecommendationExpirationService.activeThreshold(LocalDateTime.now()),
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .map(GroupRecommendation::getStatus)
                .orElse(null);
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

    private String toRecommendationContextJson(GroupRoom room) {
        Map<String, Object> context = new LinkedHashMap<>();
        GroupLocation location = latestGroupLocation(room.getId());
        if (location == null) {
            return "{}";
        }
        if (location.getLatitude() != null) {
            context.put("latitude", location.getLatitude());
        }
        if (location.getLongitude() != null) {
            context.put("longitude", location.getLongitude());
        }
        if (location.getRadiusMeters() != null) {
            context.put("radiusMeters", location.getRadiusMeters());
        }
        if (location.getAddress() != null) {
            context.put("address", location.getAddress());
        }

        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("그룹 추천 컨텍스트 정보를 JSON으로 변환할 수 없습니다.", exception);
        }
    }

    private void updateRoomLocationFromContextJson(GroupRoom room, String contextJson) {
        if (contextJson == null || contextJson.isBlank()) {
            return;
        }

        try {
            JsonNode contextNode = objectMapper.readTree(contextJson);
            if (contextNode == null || !contextNode.isObject()) {
                return;
            }

            BigDecimal latitude = toLocationDecimal(contextNode.get("latitude"), new BigDecimal("-90"),
                    new BigDecimal("90"));
            BigDecimal longitude = toLocationDecimal(contextNode.get("longitude"), new BigDecimal("-180"),
                    new BigDecimal("180"));
            Integer radiusMeters = toRadiusMeters(contextNode.get("radiusMeters"));
            String address = toLocationAddress(contextNode.get("address"));
            updateLatestGroupLocation(room, latitude, longitude, radiusMeters, address);
        } catch (JsonProcessingException ignored) {
            // contextJson is an open-ended recommendation snapshot; invalid location data must not break creation.
        }
    }

    private GroupLocation updateLatestGroupLocation(
            GroupRoom room,
            BigDecimal latitude,
            BigDecimal longitude,
            Integer radiusMeters,
            String address
    ) {
        if (latitude == null && longitude == null && radiusMeters == null && address == null) {
            return latestGroupLocation(room.getId());
        }

        GroupLocation location = latestGroupLocation(room.getId());
        if (location == null) {
            return groupLocationRepository.save(new GroupLocation(room, latitude, longitude, radiusMeters, address));
        }

        location.update(latitude, longitude, radiusMeters, address);
        return location;
    }

    private GroupLocation latestGroupLocation(Long roomId) {
        return groupLocationRepository.findFirstByRoomIdOrderByCreatedAtDescIdDesc(roomId).orElse(null);
    }

    private BigDecimal toLocationDecimal(JsonNode valueNode, BigDecimal min, BigDecimal max) {
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }

        try {
            BigDecimal value = valueNode.isNumber()
                    ? valueNode.decimalValue()
                    : new BigDecimal(valueNode.asText());
            if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
                return null;
            }

            return value;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer toRadiusMeters(JsonNode valueNode) {
        if (valueNode == null || valueNode.isNull() || !valueNode.canConvertToInt()) {
            return null;
        }

        int value = valueNode.intValue();
        return value >= 0 ? value : null;
    }

    private String toLocationAddress(JsonNode valueNode) {
        if (valueNode == null || valueNode.isNull() || !valueNode.isTextual()) {
            return null;
        }

        String address = valueNode.textValue();
        if (address.isBlank()) {
            return null;
        }

        return address;
    }

    private String createUniqueInviteCode() {
        for (int attempt = 0; attempt < MAX_INVITE_CODE_GENERATION_ATTEMPTS; attempt++) {
            String inviteCode = groupInviteCodeGenerator.generate();

            if (!groupRoomRepository.existsByInviteCode(inviteCode)) {
                return inviteCode;
            }
        }

        throw new BusinessException(GroupErrorCode.INVITE_CODE_GENERATION_FAILED);
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

    private void revokeActiveInvites(GroupRoom room) {
        groupInviteRepository.findAllByRoomIdAndStatus(room.getId(), GroupInviteStatus.PENDING)
                .forEach(groupInvite -> groupInvite.revoke());
    }

    private void leaveActiveMembers(GroupRoom room, LocalDateTime leftAt) {
        groupRoomMemberRepository.findActiveMembersByRoomId(room.getId())
                .forEach(membership -> membership.leave(leftAt));
    }

    private GroupMemberSummaryResult toMemberSummaryResult(GroupRoomMember membership, Long currentMemberId) {
        Member member = membership.getMember();

        return new GroupMemberSummaryResult(
                member.getId(),
                member.getNickname(),
                membership.getRole(),
                membership.getStatus(),
                membership.getJoinedAt(),
                member.getId().equals(currentMemberId)
        );
    }
}
