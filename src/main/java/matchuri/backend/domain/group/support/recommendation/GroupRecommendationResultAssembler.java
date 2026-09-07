package matchuri.backend.domain.group.support.recommendation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.image.support.ImageUrlResolver;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationCandidate;
import matchuri.backend.domain.group.entity.GroupRecommendationReadiness;
import matchuri.backend.domain.group.entity.GroupRecommendationReadinessStatus;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import matchuri.backend.domain.group.entity.GroupRecommendationVote;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.repository.GroupCandidateVoteCountProjection;
import matchuri.backend.domain.group.repository.GroupRecommendationCandidateRepository;
import matchuri.backend.domain.group.repository.GroupRecommendationReadinessRepository;
import matchuri.backend.domain.group.repository.GroupRecommendationVoteRepository;
import matchuri.backend.domain.group.repository.GroupRecommendationVoteQueryRow;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.result.GroupMemberVoteResult;
import matchuri.backend.domain.group.result.GroupRecommendationCandidateResult;
import matchuri.backend.domain.group.result.GroupRecommendationReadinessMemberResult;
import matchuri.backend.domain.group.result.GroupRecommendationReadinessProgressResult;
import matchuri.backend.domain.group.result.GroupRecommendationResult;
import matchuri.backend.domain.group.result.GroupVoteProgressResult;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.support.MenuThumbnailUrlResolver;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupRecommendationResultAssembler {

    private final GroupRecommendationCandidateRepository groupRecommendationCandidateRepository;
    private final GroupRecommendationReadinessRepository groupRecommendationReadinessRepository;
    private final GroupRecommendationVoteRepository groupRecommendationVoteRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final MenuThumbnailUrlResolver menuThumbnailUrlResolver;
    private final ImageUrlResolver imageUrlResolver;
    private final ObjectMapper objectMapper;

    public GroupRecommendationReadinessProgressResult readinessProgress(
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

    public GroupRecommendationReadinessMemberResult toReadinessMemberResult(
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

    public GroupRecommendationResult toGroupRecommendationResult(
            GroupRecommendation recommendation,
            Long currentMemberId
    ) {
        boolean preparing = recommendation.getStatus() == GroupRecommendationStatus.PREPARING;
        List<GroupRecommendationCandidateResult> candidates =
                preparing ? List.of() : toCandidateResults(recommendation);
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

    public GroupRecommendationResult toGroupRecommendationResult(
            GroupRecommendation recommendation,
            Long currentMemberId,
            List<GroupRoomMember> activeMemberships
    ) {
        boolean preparing = recommendation.getStatus() == GroupRecommendationStatus.PREPARING;
        List<GroupRecommendationCandidateResult> candidates =
                preparing ? List.of() : toProjectedCandidateResults(recommendation.getId());
        List<GroupRecommendationVoteQueryRow> voteRows = preparing
                ? List.of()
                : groupRecommendationVoteRepository.findVoteRowsByRecommendationId(recommendation.getId());
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
                                activeMemberships.size()
                        )
                        : null,
                candidates,
                preparing ? null : new GroupVoteProgressResult(activeMemberships.size(), voteRows.size()),
                preparing ? List.of() : toProjectedMemberVoteResults(
                        activeMemberships,
                        voteRows,
                        currentMemberId
                ),
                finalCandidate,
                recommendation.getCreatedAt()
        );
    }

    private List<GroupRecommendationCandidateResult> toProjectedCandidateResults(Long recommendationId) {
        return groupRecommendationCandidateRepository.findCandidateRowsWithVoteCounts(recommendationId).stream()
                .map(row -> new GroupRecommendationCandidateResult(
                        row.candidateId(),
                        row.menuId(),
                        row.menuName(),
                        row.thumbnailObjectKey() == null
                                ? null
                                : imageUrlResolver.toPublicUrl(row.thumbnailObjectKey()),
                        row.rankNo(),
                        row.score(),
                        row.voteCount().intValue()
                ))
                .toList();
    }

    private List<GroupMemberVoteResult> toProjectedMemberVoteResults(
            List<GroupRoomMember> activeMemberships,
            List<GroupRecommendationVoteQueryRow> voteRows,
            Long currentMemberId
    ) {
        Map<Long, Long> candidateIdByMemberId = voteRows.stream()
                .collect(Collectors.toMap(
                        GroupRecommendationVoteQueryRow::memberId,
                        GroupRecommendationVoteQueryRow::candidateId
                ));

        return activeMemberships.stream()
                .map(membership -> {
                    Member member = membership.getMember();
                    Long candidateId = candidateIdByMemberId.get(member.getId());

                    return new GroupMemberVoteResult(
                            member.getId(),
                            member.getNickname(),
                            membership.getRole(),
                            member.getId().equals(currentMemberId),
                            candidateId != null,
                            candidateId
                    );
                })
                .toList();
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

    public List<GroupRecommendationCandidateResult> toCandidateResults(GroupRecommendation recommendation) {
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

    public List<GroupRecommendationCandidateResult> toCandidateResults(
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

    public Map<Long, Integer> countVotesByCandidateId(Long recommendationId) {
        return groupRecommendationVoteRepository.countVotesByCandidateId(recommendationId)
                .stream()
                .collect(Collectors.toMap(
                        GroupCandidateVoteCountProjection::getCandidateId,
                        projection -> projection.getVoteCount().intValue()
                ));
    }

    public GroupVoteProgressResult toVoteProgress(GroupRecommendation recommendation) {
        int totalMemberCount = groupRoomMemberRepository.findActiveMembersByRoomId(recommendation.getRoom().getId())
                .size();
        int votedMemberCount = Math.toIntExact(
                groupRecommendationVoteRepository.countByGroupRecommendationId(recommendation.getId())
        );

        return new GroupVoteProgressResult(totalMemberCount, votedMemberCount);
    }
}





