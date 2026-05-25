package matchuri.backend.api.group;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.group.dto.request.CreateGroupRecommendationRequest;
import matchuri.backend.api.group.dto.request.CreateGroupRequest;
import matchuri.backend.api.group.dto.request.CreateNicknameGroupInviteRequest;
import matchuri.backend.api.group.dto.request.JoinGroupRequest;
import matchuri.backend.api.group.dto.request.RespondGroupInviteRequest;
import matchuri.backend.api.group.dto.request.RerollGroupRecommendationRequest;
import matchuri.backend.api.group.dto.request.UpdateGroupRequest;
import matchuri.backend.api.group.dto.request.VoteGroupRecommendationRequest;
import matchuri.backend.api.group.dto.response.CreateGroupRecommendationResponse;
import matchuri.backend.api.group.dto.response.CreateGroupResponse;
import matchuri.backend.api.group.dto.response.CreateNicknameGroupInviteResponse;
import matchuri.backend.api.group.dto.response.DeleteGroupResponse;
import matchuri.backend.api.group.dto.response.FinalizeGroupRecommendationResponse;
import matchuri.backend.api.group.dto.response.GroupDetailResponse;
import matchuri.backend.api.group.dto.response.GroupInviteSummaryResponse;
import matchuri.backend.api.group.dto.response.GroupRecommendationCandidateListResponse;
import matchuri.backend.api.group.dto.response.GroupRecommendationSessionResponse;
import matchuri.backend.api.group.dto.response.GroupSummaryResponse;
import matchuri.backend.api.group.dto.response.GroupVoteResponse;
import matchuri.backend.api.group.dto.response.JoinGroupResponse;
import matchuri.backend.api.group.dto.response.LeaveGroupResponse;
import matchuri.backend.api.group.dto.response.RespondGroupInviteResponse;
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
import matchuri.backend.domain.group.result.GroupDetailResult;
import matchuri.backend.domain.group.result.GroupInviteSummaryResult;
import matchuri.backend.domain.group.result.GroupRecommendationCandidateListResult;
import matchuri.backend.domain.group.result.GroupRecommendationResult;
import matchuri.backend.domain.group.result.GroupSummaryResult;
import matchuri.backend.domain.group.result.JoinGroupResult;
import matchuri.backend.domain.group.result.LeaveGroupResult;
import matchuri.backend.domain.group.result.RespondGroupInviteResult;
import matchuri.backend.domain.group.result.UpdateGroupResult;
import matchuri.backend.domain.group.service.GroupService;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.api.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/groups")
@Validated
@RequiredArgsConstructor
public class GroupController implements GroupApi {

    private final GroupService groupService;
    private final GroupMapper groupMapper;

    @Override
    @PostMapping
    public ApiResponse<CreateGroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        CreateGroupCommand command = groupMapper.toCreateGroupCommand(request);
        CreateGroupResult result = groupService.createGroup(command);

        return ApiResponse.success(groupMapper.toCreateGroupResponse(result));
    }

    @Override
    @GetMapping
    public ApiResponse<PageResponse<GroupSummaryResponse>> getMyGroups(
            @RequestParam(required = false) GroupRoomStatus status,
            @Min(0) @RequestParam(defaultValue = "0")
            Integer page,

            @Min(1) @Max(100) @RequestParam(defaultValue = "20")
            Integer size
    ) {
        GetMyGroupsCommand command = groupMapper.toGetMyGroupsCommand(status, page, size);
        Page<@NonNull GroupSummaryResult> results = groupService.getMyGroups(command);
        PageResponse<GroupSummaryResponse> response = PageResponse.of(results, groupMapper::toGroupSummaryResponse);

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/{groupId}")
    public ApiResponse<GroupDetailResponse> getGroup(@PathVariable Long groupId) {
        GroupDetailResult result = groupService.getGroup(groupId);

        return ApiResponse.success(groupMapper.toGroupDetailResponse(result));
    }

    @Override
    @PostMapping("/invites/nickname")
    public ApiResponse<CreateNicknameGroupInviteResponse> createNicknameInvite(
            @Valid @RequestBody CreateNicknameGroupInviteRequest request
    ) {
        CreateNicknameGroupInviteCommand command = groupMapper.toCreateNicknameGroupInviteCommand(request);
        CreateNicknameGroupInviteResult result = groupService.createNicknameInvite(command);

        return ApiResponse.success(groupMapper.toCreateNicknameGroupInviteResponse(result));
    }

    @Override
    @GetMapping("/invites/me")
    public ApiResponse<PageResponse<GroupInviteSummaryResponse>> getMyInvites(
            @RequestParam(required = false) GroupInviteStatus status,
            @Min(0) @RequestParam(defaultValue = "0")
            Integer page,

            @Min(1) @Max(100) @RequestParam(defaultValue = "20")
            Integer size
    ) {
        GetMyGroupInvitesCommand command = groupMapper.toGetMyGroupInvitesCommand(status, page, size);
        Page<@NonNull GroupInviteSummaryResult> results = groupService.getMyInvites(command);
        PageResponse<GroupInviteSummaryResponse> response =
                PageResponse.of(results, groupMapper::toGroupInviteSummaryResponse);

        return ApiResponse.success(response);
    }

    @Override
    @PostMapping("/invites/{inviteId}/response")
    public ApiResponse<RespondGroupInviteResponse> respondGroupInvite(
            @PathVariable Long inviteId,
            @Valid @RequestBody RespondGroupInviteRequest request
    ) {
        RespondGroupInviteCommand command = groupMapper.toRespondGroupInviteCommand(inviteId, request);
        RespondGroupInviteResult result = groupService.respondGroupInvite(command);

        return ApiResponse.success(groupMapper.toRespondGroupInviteResponse(result));
    }

    @Override
    @PatchMapping("/{groupId}")
    public ApiResponse<UpdateGroupResponse> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupRequest request
    ) {
        UpdateGroupCommand command = groupMapper.toUpdateGroupCommand(groupId, request);
        UpdateGroupResult result = groupService.updateGroup(command);

        return ApiResponse.success(groupMapper.toUpdateGroupResponse(result));
    }

    @Override
    @PostMapping("/join")
    public ApiResponse<JoinGroupResponse> joinGroup(@Valid @RequestBody JoinGroupRequest request) {
        JoinGroupCommand command = groupMapper.toJoinGroupCommand(request);
        JoinGroupResult result = groupService.joinGroup(command);

        return ApiResponse.success(groupMapper.toJoinGroupResponse(result));
    }

    @Override
    @PostMapping("/{groupId}/leave")
    public ApiResponse<LeaveGroupResponse> leaveGroup(@PathVariable Long groupId) {
        LeaveGroupCommand command = groupMapper.toLeaveGroupCommand(groupId);
        LeaveGroupResult result = groupService.leaveGroup(command);

        return ApiResponse.success(groupMapper.toLeaveGroupResponse(result));
    }

    @Override
    @DeleteMapping("/{groupId}")
    public ApiResponse<DeleteGroupResponse> deleteGroup(@PathVariable Long groupId) {
        DeleteGroupCommand command = groupMapper.toDeleteGroupCommand(groupId);
        DeleteGroupResult result = groupService.deleteGroup(command);

        return ApiResponse.success(groupMapper.toDeleteGroupResponse(result));
    }

    @Override
    @PostMapping("/{groupId}/recommendations")
    public ApiResponse<CreateGroupRecommendationResponse> createRecommendation(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateGroupRecommendationRequest request
    ) {
        CreateGroupRecommendationCommand command = groupMapper.toCreateGroupRecommendationCommand(groupId, request);
        CreateGroupRecommendationResult result = groupService.createGroupRecommendation(command);

        return ApiResponse.success(groupMapper.toCreateGroupRecommendationResponse(result));
    }

    @Override
    @GetMapping("/{groupId}/recommendations/{sessionId}")
    public ApiResponse<GroupRecommendationSessionResponse> getRecommendation(
            @PathVariable Long groupId,
            @PathVariable Long sessionId
    ) {
        GroupRecommendationResult result = groupService.getGroupRecommendation(groupId, sessionId);

        return ApiResponse.success(groupMapper.toGroupRecommendationSessionResponse(result));
    }

    @Override
    @GetMapping("/{groupId}/recommendations/{sessionId}/candidates")
    public ApiResponse<GroupRecommendationCandidateListResponse> getRecommendationCandidates(
            @PathVariable Long groupId,
            @PathVariable Long sessionId
    ) {
        GroupRecommendationCandidateListResult result =
                groupService.getGroupRecommendationCandidates(groupId, sessionId);

        return ApiResponse.success(groupMapper.toGroupRecommendationCandidateListResponse(result));
    }

    @Override
    @PostMapping("/{groupId}/recommendations/{sessionId}/reroll")
    public ApiResponse<CreateGroupRecommendationResponse> rerollRecommendation(
            @PathVariable Long groupId,
            @PathVariable Long sessionId,
            @Valid @RequestBody RerollGroupRecommendationRequest request
    ) {
        CreateGroupRecommendationCommand command = groupMapper.toCreateGroupRecommendationCommand(groupId, request);
        CreateGroupRecommendationResult result = groupService.rerollGroupRecommendation(
                command.groupId(),
                sessionId,
                request.rerollType(),
                command.contextJson()
        );

        return ApiResponse.success(groupMapper.toCreateGroupRecommendationResponse(result));
    }

    @Override
    @PostMapping("/{groupId}/recommendations/{sessionId}/votes")
    public ApiResponse<GroupVoteResponse> vote(
            @PathVariable Long groupId,
            @PathVariable Long sessionId,
            @Valid @RequestBody VoteGroupRecommendationRequest request
    ) {
        return ApiResponse.success(GroupVoteResponse.mockVoted(request.candidateId(), request.voteValue()));
    }

    @Override
    @PatchMapping("/{groupId}/recommendations/{sessionId}/finalize")
    public ApiResponse<FinalizeGroupRecommendationResponse> finalizeRecommendation(
            @PathVariable Long groupId,
            @PathVariable Long sessionId
    ) {
        return ApiResponse.success(FinalizeGroupRecommendationResponse.mockFinalized());
    }
}
