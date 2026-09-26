package matchuri.backend.domain.group.service;

import java.util.List;
import lombok.NonNull;
import matchuri.backend.domain.group.command.CreateGroupRecommendationCommand;
import matchuri.backend.domain.group.command.FinalizeGroupRecommendationCommand;
import matchuri.backend.domain.group.entity.GroupRecommendationRerollType;
import matchuri.backend.domain.group.result.CreateGroupRecommendationResult;
import matchuri.backend.domain.group.result.FinalizeGroupRecommendationResult;
import matchuri.backend.domain.group.result.GroupHomeActivityResult;
import matchuri.backend.domain.group.result.GroupRecommendationCandidateListResult;
import matchuri.backend.domain.group.result.GroupRecommendationReadinessResult;
import matchuri.backend.domain.group.result.GroupRecommendationDetailResult;
import matchuri.backend.domain.group.result.GroupRecommendationResult;
import matchuri.backend.domain.group.result.GroupRecommendationSummaryResult;
import matchuri.backend.domain.group.result.GroupRecommendationV2SummaryResult;
import matchuri.backend.domain.group.result.GroupVoteResult;
import matchuri.backend.domain.group.result.ReadyGroupRecommendationResult;
import org.springframework.data.domain.Page;

public interface GroupRecommendationService {

    List<GroupHomeActivityResult> getHomeActivities(Long memberId);

    CreateGroupRecommendationResult createGroupRecommendation(Long memberId, CreateGroupRecommendationCommand command);

    CreateGroupRecommendationResult rerollGroupRecommendation(Long memberId, Long groupId, Long sessionId, GroupRecommendationRerollType rerollType, String contextJson);

    GroupRecommendationDetailResult getGroupRecommendation(Long memberId, Long groupId, Long sessionId);

    GroupRecommendationCandidateListResult getGroupRecommendationCandidates(Long memberId, Long groupId, Long sessionId);

    Page<@NonNull GroupRecommendationSummaryResult> getGroupRecommendations(Long memberId, Long groupId, int page, int size);

    Page<@NonNull GroupRecommendationV2SummaryResult> getGroupRecommendationsV2(
            Long memberId,
            Long groupId,
            int page,
            int size
    );

    GroupRecommendationReadinessResult getGroupRecommendationReadiness(Long memberId, Long groupId, Long sessionId);

    ReadyGroupRecommendationResult readyGroupRecommendation(Long memberId, Long groupId, Long sessionId);

    GroupVoteResult voteGroupRecommendation(Long memberId, Long groupId, Long sessionId, Long candidateId);

    FinalizeGroupRecommendationResult finalizeGroupRecommendation(Long memberId, FinalizeGroupRecommendationCommand command);
}
