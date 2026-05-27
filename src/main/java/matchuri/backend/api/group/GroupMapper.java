package matchuri.backend.api.group;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.group.dto.request.CreateGroupRecommendationRequest;
import matchuri.backend.api.group.dto.request.CreateGroupRequest;
import matchuri.backend.api.group.dto.request.CreateNicknameGroupInviteRequest;
import matchuri.backend.api.group.dto.request.JoinGroupRequest;
import matchuri.backend.api.group.dto.request.RespondGroupInviteRequest;
import matchuri.backend.api.group.dto.request.RerollGroupRecommendationRequest;
import matchuri.backend.api.group.dto.request.UpdateGroupRequest;
import matchuri.backend.api.group.dto.response.CreateNicknameGroupInviteResponse;
import matchuri.backend.api.group.dto.response.CreateGroupResponse;
import matchuri.backend.api.group.dto.response.CreateGroupRecommendationResponse;
import matchuri.backend.api.group.dto.response.DeleteGroupResponse;
import matchuri.backend.api.group.dto.response.FinalizeGroupRecommendationResponse;
import matchuri.backend.api.group.dto.response.GroupDetailResponse;
import matchuri.backend.api.group.dto.response.GroupInviteSummaryResponse;
import matchuri.backend.api.group.dto.response.GroupMemberSummaryResponse;
import matchuri.backend.api.group.dto.response.GroupRecommendationCandidateListResponse;
import matchuri.backend.api.group.dto.response.GroupRecommendationCandidateResponse;
import matchuri.backend.api.group.dto.response.GroupRecommendationReadinessMemberResponse;
import matchuri.backend.api.group.dto.response.GroupRecommendationReadinessResponse;
import matchuri.backend.api.group.dto.response.GroupRecommendationSessionResponse;
import matchuri.backend.api.group.dto.response.GroupRecommendationSummaryResponse;
import matchuri.backend.api.group.dto.response.GroupRecommendationReadinessProgressResponse;
import matchuri.backend.api.group.dto.response.GroupSummaryResponse;
import matchuri.backend.api.group.dto.response.GroupVoteProgressResponse;
import matchuri.backend.api.group.dto.response.GroupVoteResponse;
import matchuri.backend.api.group.dto.response.JoinGroupResponse;
import matchuri.backend.api.group.dto.response.LeaveGroupResponse;
import matchuri.backend.api.group.dto.response.RespondGroupInviteResponse;
import matchuri.backend.api.group.dto.response.ReadyGroupRecommendationResponse;
import matchuri.backend.api.group.dto.response.UpdateGroupResponse;
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
import matchuri.backend.domain.group.entity.GroupInviteStatus;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.result.CreateGroupResult;
import matchuri.backend.domain.group.result.CreateGroupRecommendationResult;
import matchuri.backend.domain.group.result.CreateNicknameGroupInviteResult;
import matchuri.backend.domain.group.result.DeleteGroupResult;
import matchuri.backend.domain.group.result.FinalizeGroupRecommendationResult;
import matchuri.backend.domain.group.result.GroupDetailResult;
import matchuri.backend.domain.group.result.GroupInviteSummaryResult;
import matchuri.backend.domain.group.result.GroupMemberSummaryResult;
import matchuri.backend.domain.group.result.GroupRecommendationCandidateListResult;
import matchuri.backend.domain.group.result.GroupRecommendationCandidateResult;
import matchuri.backend.domain.group.result.GroupRecommendationReadinessMemberResult;
import matchuri.backend.domain.group.result.GroupRecommendationReadinessResult;
import matchuri.backend.domain.group.result.GroupRecommendationResult;
import matchuri.backend.domain.group.result.GroupRecommendationSummaryResult;
import matchuri.backend.domain.group.result.GroupRecommendationReadinessProgressResult;
import matchuri.backend.domain.group.result.GroupSummaryResult;
import matchuri.backend.domain.group.result.GroupVoteProgressResult;
import matchuri.backend.domain.group.result.GroupVoteResult;
import matchuri.backend.domain.group.result.JoinGroupResult;
import matchuri.backend.domain.group.result.LeaveGroupResult;
import matchuri.backend.domain.group.result.RespondGroupInviteResult;
import matchuri.backend.domain.group.result.ReadyGroupRecommendationResult;
import matchuri.backend.domain.group.result.UpdateGroupResult;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupMapper {

    private final ObjectMapper objectMapper;

    public CreateGroupCommand toCreateGroupCommand(CreateGroupRequest request) {
        return new CreateGroupCommand(
                request.name(),
                request.latitude(),
                request.longitude()
        );
    }

    public CreateGroupResponse toCreateGroupResponse(CreateGroupResult result) {
        return new CreateGroupResponse(
                result.groupId(),
                result.inviteCode(),
                result.status()
        );
    }

    public CreateGroupRecommendationCommand toCreateGroupRecommendationCommand(
            Long groupId,
            CreateGroupRecommendationRequest request
    ) {
        return new CreateGroupRecommendationCommand(groupId, toContextJson(request.contextJson()));
    }

    public CreateGroupRecommendationCommand toCreateGroupRecommendationCommand(
            Long groupId,
            RerollGroupRecommendationRequest request
    ) {
        return new CreateGroupRecommendationCommand(groupId, toContextJson(request.contextJson()));
    }

    public CreateGroupRecommendationResponse toCreateGroupRecommendationResponse(
            CreateGroupRecommendationResult result
    ) {
        return new CreateGroupRecommendationResponse(
                result.sessionId(),
                result.status(),
                result.candidates().stream()
                        .map(this::toGroupRecommendationCandidateResponse)
                        .toList()
        );
    }

    public CreateNicknameGroupInviteCommand toCreateNicknameGroupInviteCommand(
            CreateNicknameGroupInviteRequest request
    ) {
        return new CreateNicknameGroupInviteCommand(
                request.groupId(),
                request.nickname()
        );
    }

    public CreateNicknameGroupInviteResponse toCreateNicknameGroupInviteResponse(
            CreateNicknameGroupInviteResult result
    ) {
        return new CreateNicknameGroupInviteResponse(
                result.inviteId(),
                result.groupId(),
                result.groupName(),
                result.targetMemberId(),
                result.targetNickname(),
                result.expiresAt(),
                result.status()
        );
    }

    public JoinGroupCommand toJoinGroupCommand(JoinGroupRequest request) {
        return new JoinGroupCommand(request.inviteCode());
    }

    public RespondGroupInviteCommand toRespondGroupInviteCommand(Long inviteId, RespondGroupInviteRequest request) {
        return new RespondGroupInviteCommand(inviteId, request.responseType());
    }

    public JoinGroupResponse toJoinGroupResponse(JoinGroupResult result) {
        return new JoinGroupResponse(
                result.groupId(),
                result.memberStatus()
        );
    }

    public RespondGroupInviteResponse toRespondGroupInviteResponse(RespondGroupInviteResult result) {
        return new RespondGroupInviteResponse(
                result.inviteId(),
                result.groupId(),
                result.inviteStatus(),
                result.memberStatus(),
                result.respondedAt()
        );
    }

    public LeaveGroupCommand toLeaveGroupCommand(Long groupId) {
        return new LeaveGroupCommand(groupId);
    }

    public LeaveGroupResponse toLeaveGroupResponse(LeaveGroupResult result) {
        return new LeaveGroupResponse(
                result.groupId(),
                result.memberStatus(),
                result.leftAt()
        );
    }

    public DeleteGroupCommand toDeleteGroupCommand(Long groupId) {
        return new DeleteGroupCommand(groupId);
    }

    public DeleteGroupResponse toDeleteGroupResponse(DeleteGroupResult result) {
        return new DeleteGroupResponse(
                result.groupId(),
                result.status(),
                result.deletedAt()
        );
    }

    public UpdateGroupCommand toUpdateGroupCommand(Long groupId, UpdateGroupRequest request) {
        return new UpdateGroupCommand(
                groupId,
                request.name(),
                request.latitude(),
                request.longitude()
        );
    }

    public UpdateGroupResponse toUpdateGroupResponse(UpdateGroupResult result) {
        return new UpdateGroupResponse(
                result.groupId(),
                result.name(),
                result.latitude(),
                result.longitude(),
                result.status(),
                result.updatedAt(),
                result.openGroupRecommendationId()
        );
    }

    public GetMyGroupsCommand toGetMyGroupsCommand(GroupRoomStatus status, int page, int size) {
        return new GetMyGroupsCommand(status, page, size);
    }

    public GetMyGroupInvitesCommand toGetMyGroupInvitesCommand(GroupInviteStatus status, int page, int size) {
        return new GetMyGroupInvitesCommand(status, page, size);
    }

    public GroupSummaryResponse toGroupSummaryResponse(GroupSummaryResult result) {
        return new GroupSummaryResponse(
                result.id(),
                result.name(),
                result.status(),
                result.memberCount(),
                result.latestRecommendationStatus(),
                result.createdAt()
        );
    }

    public GroupDetailResponse toGroupDetailResponse(GroupDetailResult result) {
        return new GroupDetailResponse(
                result.id(),
                result.name(),
                result.inviteCode(),
                result.latitude(),
                result.longitude(),
                result.status(),
                result.members().stream()
                        .map(this::toGroupMemberSummaryResponse)
                        .toList(),
                result.activeRecommendation() == null
                        ? null
                        : toGroupRecommendationSessionResponse(result.activeRecommendation())
        );
    }

    public GroupRecommendationSessionResponse toGroupRecommendationSessionResponse(
            GroupRecommendationResult result
    ) {
        return new GroupRecommendationSessionResponse(
                result.sessionId(),
                result.status(),
                result.readiness() == null
                        ? null
                        : toGroupRecommendationReadinessProgressResponse(result.readiness()),
                result.candidates().stream()
                        .map(this::toGroupRecommendationCandidateResponse)
                        .toList(),
                result.voteProgress() == null
                        ? null
                        : toGroupVoteProgressResponse(result.voteProgress()),
                result.finalCandidate() == null
                        ? null
                        : toGroupRecommendationCandidateResponse(result.finalCandidate()),
                result.createdAt()
        );
    }

    public GroupRecommendationCandidateListResponse toGroupRecommendationCandidateListResponse(
            GroupRecommendationCandidateListResult result
    ) {
        return new GroupRecommendationCandidateListResponse(
                result.sessionId(),
                result.candidates().stream()
                        .map(this::toGroupRecommendationCandidateResponse)
                        .toList()
        );
    }

    public GroupRecommendationSummaryResponse toGroupRecommendationSummaryResponse(
            GroupRecommendationSummaryResult result
    ) {
        return new GroupRecommendationSummaryResponse(
                result.sessionId(),
                result.status(),
                result.startedAt(),
                result.endedAt()
        );
    }

    public GroupRecommendationReadinessResponse toGroupRecommendationReadinessResponse(
            GroupRecommendationReadinessResult result
    ) {
        return new GroupRecommendationReadinessResponse(
                result.sessionId(),
                result.status(),
                toGroupRecommendationReadinessProgressResponse(result.progress()),
                result.members().stream()
                        .map(this::toGroupRecommendationReadinessMemberResponse)
                        .toList()
        );
    }

    public ReadyGroupRecommendationResponse toReadyGroupRecommendationResponse(
            ReadyGroupRecommendationResult result
    ) {
        return new ReadyGroupRecommendationResponse(
                result.sessionId(),
                result.status(),
                toGroupRecommendationReadinessProgressResponse(result.readiness()),
                result.candidates().stream()
                        .map(this::toGroupRecommendationCandidateResponse)
                        .toList()
        );
    }

    public GroupVoteResponse toGroupVoteResponse(GroupVoteResult result) {
        return new GroupVoteResponse(
                result.voteId(),
                result.candidateId(),
                result.votedAt()
        );
    }

    public FinalizeGroupRecommendationResponse toFinalizeGroupRecommendationResponse(
            FinalizeGroupRecommendationResult result
    ) {
        return new FinalizeGroupRecommendationResponse(
                result.sessionId(),
                result.status(),
                toGroupRecommendationCandidateResponse(result.finalCandidate()),
                result.finalizedAt()
        );
    }

    public GroupInviteSummaryResponse toGroupInviteSummaryResponse(GroupInviteSummaryResult result) {
        return new GroupInviteSummaryResponse(
                result.inviteId(),
                result.groupId(),
                result.groupName(),
                result.requestMemberId(),
                result.requestMemberNickname(),
                result.status(),
                result.expiresAt(),
                result.createdAt()
        );
    }

    private GroupMemberSummaryResponse toGroupMemberSummaryResponse(GroupMemberSummaryResult result) {
        return new GroupMemberSummaryResponse(
                result.memberId(),
                result.nickname(),
                result.role(),
                result.status(),
                result.joinedAt()
        );
    }

    private GroupRecommendationCandidateResponse toGroupRecommendationCandidateResponse(
            GroupRecommendationCandidateResult result
    ) {
        return new GroupRecommendationCandidateResponse(
                result.candidateId(),
                result.menuId(),
                result.menuName(),
                result.rankNo(),
                result.score(),
                result.voteCount()
        );
    }

    private GroupVoteProgressResponse toGroupVoteProgressResponse(GroupVoteProgressResult result) {
        return new GroupVoteProgressResponse(
                result.totalMemberCount(),
                result.votedMemberCount()
        );
    }

    private GroupRecommendationReadinessMemberResponse toGroupRecommendationReadinessMemberResponse(
            GroupRecommendationReadinessMemberResult result
    ) {
        return new GroupRecommendationReadinessMemberResponse(
                result.memberId(),
                result.nickname(),
                result.role(),
                result.ready(),
                result.readinessStatus(),
                result.readinessUpdatedAt()
        );
    }

    private GroupRecommendationReadinessProgressResponse toGroupRecommendationReadinessProgressResponse(
            GroupRecommendationReadinessProgressResult result
    ) {
        return new GroupRecommendationReadinessProgressResponse(
                result.totalMemberCount(),
                result.readyMemberCount(),
                result.allReady()
        );
    }

    private String toContextJson(Map<String, Object> contextJson) {
        try {
            Map<String, Object> normalizedContextJson = contextJson == null ? Map.of() : contextJson;

            return objectMapper.writeValueAsString(normalizedContextJson);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("contextJson을 JSON 문자열로 변환할 수 없습니다.", exception);
        }
    }
}
