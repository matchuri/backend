package matchuri.backend.domain.group.service;

import lombok.NonNull;
import matchuri.backend.domain.group.command.CreateGroupCommand;
import matchuri.backend.domain.group.command.CreateGroupRecommendationCommand;
import matchuri.backend.domain.group.command.CreateNicknameGroupInviteCommand;
import matchuri.backend.domain.group.command.DeleteGroupCommand;
import matchuri.backend.domain.group.command.GetMyGroupInvitesCommand;
import matchuri.backend.domain.group.command.GetMyGroupsCommand;
import matchuri.backend.domain.group.command.JoinGroupCommand;
import matchuri.backend.domain.group.command.LeaveGroupCommand;
import matchuri.backend.domain.group.command.RespondGroupInviteCommand;
import matchuri.backend.domain.group.command.UpdateGroupCommand;
import matchuri.backend.domain.group.entity.GroupRecommendationRerollType;
import matchuri.backend.domain.group.result.CreateGroupResult;
import matchuri.backend.domain.group.result.CreateGroupRecommendationResult;
import matchuri.backend.domain.group.result.CreateNicknameGroupInviteResult;
import matchuri.backend.domain.group.result.DeleteGroupResult;
import matchuri.backend.domain.group.result.FinalizeGroupRecommendationResult;
import matchuri.backend.domain.group.result.GroupDetailResult;
import matchuri.backend.domain.group.result.GroupInviteSummaryResult;
import matchuri.backend.domain.group.result.GroupRecommendationCandidateListResult;
import matchuri.backend.domain.group.result.GroupRecommendationResult;
import matchuri.backend.domain.group.result.GroupRecommendationSummaryResult;
import matchuri.backend.domain.group.result.GroupSummaryResult;
import matchuri.backend.domain.group.result.GroupVoteResult;
import matchuri.backend.domain.group.result.JoinGroupResult;
import matchuri.backend.domain.group.result.LeaveGroupResult;
import matchuri.backend.domain.group.result.RespondGroupInviteResult;
import matchuri.backend.domain.group.result.ReadyGroupRecommendationResult;
import matchuri.backend.domain.group.result.UpdateGroupResult;
import org.springframework.data.domain.Page;

public interface GroupService {

    CreateGroupResult createGroup(CreateGroupCommand command);

    CreateGroupRecommendationResult createGroupRecommendation(CreateGroupRecommendationCommand command);

    CreateGroupRecommendationResult rerollGroupRecommendation(
            Long groupId,
            Long sessionId,
            GroupRecommendationRerollType rerollType,
            String contextJson
    );

    GroupRecommendationResult getGroupRecommendation(Long groupId, Long sessionId);

    GroupRecommendationCandidateListResult getGroupRecommendationCandidates(Long groupId, Long sessionId);

    Page<GroupRecommendationSummaryResult> getGroupRecommendations(Long groupId, int page, int size);

    ReadyGroupRecommendationResult readyGroupRecommendation(Long groupId, Long sessionId);

    GroupVoteResult voteGroupRecommendation(Long groupId, Long sessionId, Long candidateId);

    FinalizeGroupRecommendationResult finalizeGroupRecommendation(Long groupId, Long sessionId);

    CreateNicknameGroupInviteResult createNicknameInvite(CreateNicknameGroupInviteCommand command);

    JoinGroupResult joinGroup(JoinGroupCommand command);

    LeaveGroupResult leaveGroup(LeaveGroupCommand command);

    DeleteGroupResult deleteGroup(DeleteGroupCommand command);

    UpdateGroupResult updateGroup(UpdateGroupCommand command);

    Page<@NonNull GroupSummaryResult> getMyGroups(GetMyGroupsCommand command);

    Page<@NonNull GroupInviteSummaryResult> getMyInvites(GetMyGroupInvitesCommand command);

    RespondGroupInviteResult respondGroupInvite(RespondGroupInviteCommand command);

    GroupDetailResult getGroup(Long groupId);
}
