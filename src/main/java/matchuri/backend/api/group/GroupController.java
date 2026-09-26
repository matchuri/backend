package matchuri.backend.api.group;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.group.dto.request.CreateGroupRecommendationRequest;
import matchuri.backend.api.group.dto.request.FinalizeGroupRecommendationRequest;
import matchuri.backend.api.group.dto.request.CreateGroupRequest;
import matchuri.backend.api.group.dto.request.CreateNicknameGroupInviteRequest;
import matchuri.backend.api.group.dto.request.JoinGroupRequest;
import matchuri.backend.api.group.dto.request.JoinGroupByInviteLinkRequest;
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
import matchuri.backend.api.group.dto.response.GroupInviteLinkResponse;
import matchuri.backend.api.group.dto.response.GroupRecommendationCandidateListResponse;
import matchuri.backend.api.group.dto.response.GroupRecommendationReadinessResponse;
import matchuri.backend.api.group.dto.response.GroupRecommendationDetailResponse;
import matchuri.backend.api.group.dto.response.GroupRecommendationSummaryResponse;
import matchuri.backend.api.group.dto.response.GroupSummaryResponse;
import matchuri.backend.api.group.dto.response.GroupVoteResponse;
import matchuri.backend.api.group.dto.response.JoinGroupResponse;
import matchuri.backend.api.group.dto.response.LeaveGroupResponse;
import matchuri.backend.api.group.dto.response.ReadyGroupRecommendationResponse;
import matchuri.backend.api.group.dto.response.RespondGroupInviteResponse;
import matchuri.backend.api.group.dto.response.UpdateGroupResponse;
import matchuri.backend.api.group.mapper.GroupMapper;
import matchuri.backend.domain.group.command.CreateGroupCommand;
import matchuri.backend.domain.group.command.CreateGroupRecommendationCommand;
import matchuri.backend.domain.group.command.CreateNicknameGroupInviteCommand;
import matchuri.backend.domain.group.command.DeleteGroupCommand;
import matchuri.backend.domain.group.command.FinalizeGroupRecommendationCommand;
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
import matchuri.backend.domain.group.result.GroupInviteLinkResult;
import matchuri.backend.domain.group.result.GroupRecommendationCandidateListResult;
import matchuri.backend.domain.group.result.GroupRecommendationDetailResult;
import matchuri.backend.domain.group.result.GroupRecommendationReadinessResult;
import matchuri.backend.domain.group.result.GroupRecommendationSummaryResult;
import matchuri.backend.domain.group.result.GroupSummaryResult;
import matchuri.backend.domain.group.result.GroupVoteResult;
import matchuri.backend.domain.group.result.JoinGroupResult;
import matchuri.backend.domain.group.result.LeaveGroupResult;
import matchuri.backend.domain.group.result.ReadyGroupRecommendationResult;
import matchuri.backend.domain.group.result.RespondGroupInviteResult;
import matchuri.backend.domain.group.result.UpdateGroupResult;
import matchuri.backend.domain.group.service.GroupInviteService;
import matchuri.backend.domain.group.service.GroupManagementService;
import matchuri.backend.domain.group.service.GroupRecommendationService;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.api.PageResponse;
import matchuri.backend.global.security.AuthenticatedMemberId;
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

    private final GroupManagementService groupManagementService;
    private final GroupInviteService groupInviteService;
    private final GroupRecommendationService groupRecommendationService;
    private final GroupMapper groupMapper;

    @Override
    @PostMapping
    public ApiResponse<CreateGroupResponse> createGroup(
            @AuthenticatedMemberId Long memberId,
            @Valid @RequestBody CreateGroupRequest request
    ) {
        CreateGroupCommand command = groupMapper.toCreateGroupCommand(request);
        CreateGroupResult result = groupManagementService.createGroup(memberId, command);

        return ApiResponse.success(groupMapper.toCreateGroupResponse(result));
    }

    @Override
    @GetMapping
    public ApiResponse<PageResponse<GroupSummaryResponse>> getMyGroups(
            @AuthenticatedMemberId Long memberId,
            @RequestParam(required = false) GroupRoomStatus status,
            @Min(0) @RequestParam(defaultValue = "0")
            Integer page,

            @Min(1) @Max(100) @RequestParam(defaultValue = "20")
            Integer size
    ) {
        GetMyGroupsCommand command = groupMapper.toGetMyGroupsCommand(status, page, size);
        Page<@NonNull GroupSummaryResult> results = groupManagementService.getMyGroups(memberId, command);
        PageResponse<GroupSummaryResponse> response = PageResponse.of(results, groupMapper::toGroupSummaryResponse);

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/{groupId}")
    public ApiResponse<GroupDetailResponse> getGroup(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId
    ) {
        GroupDetailResult result = groupManagementService.getGroup(memberId, groupId);

        return ApiResponse.success(groupMapper.toGroupDetailResponse(result));
    }

    @Override
    @PostMapping("/{groupId}/invite-link")
    public ApiResponse<GroupInviteLinkResponse> createInviteLink(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId
    ) {
        GroupInviteLinkResult result = groupInviteService.createInviteLink(memberId, groupId);
        return ApiResponse.success(groupMapper.toGroupInviteLinkResponse(result));
    }

    @Override
    @PostMapping("/{groupId}/invite-link/reissue")
    public ApiResponse<GroupInviteLinkResponse> reissueInviteLink(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId
    ) {
        GroupInviteLinkResult result = groupInviteService.reissueInviteLink(memberId, groupId);
        return ApiResponse.success(groupMapper.toGroupInviteLinkResponse(result));
    }

    @Override
    @GetMapping("/{groupId}/invite-link")
    public ApiResponse<GroupInviteLinkResponse> getCurrentInviteLink(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId
    ) {
        Optional<GroupInviteLinkResult> result = groupInviteService.getCurrentInviteLink(memberId, groupId);
        return ApiResponse.success(result.map(groupMapper::toGroupInviteLinkResponse).orElse(null));
    }

    @Override
    @PostMapping("/invites/nickname")
    public ApiResponse<CreateNicknameGroupInviteResponse> createNicknameInvite(
            @AuthenticatedMemberId Long memberId,
            @Valid @RequestBody CreateNicknameGroupInviteRequest request
    ) {
        CreateNicknameGroupInviteCommand command = groupMapper.toCreateNicknameGroupInviteCommand(request);
        CreateNicknameGroupInviteResult result = groupInviteService.createNicknameInvite(memberId, command);

        return ApiResponse.success(groupMapper.toCreateNicknameGroupInviteResponse(result));
    }

    @Override
    @Deprecated
    @GetMapping("/invites/me")
    public ApiResponse<PageResponse<GroupInviteSummaryResponse>> getMyInvites(
            @AuthenticatedMemberId Long memberId,
            @RequestParam(required = false) GroupInviteStatus status,
            @Min(0) @RequestParam(defaultValue = "0")
            Integer page,

            @Min(1) @Max(100) @RequestParam(defaultValue = "20")
            Integer size
    ) {
        GetMyGroupInvitesCommand command = groupMapper.toGetMyGroupInvitesCommand(status, page, size);
        Page<@NonNull GroupInviteSummaryResult> results = groupInviteService.getMyInvites(memberId, command);
        PageResponse<GroupInviteSummaryResponse> response =
                PageResponse.of(results, groupMapper::toGroupInviteSummaryResponse);

        return ApiResponse.success(response);
    }

    @Override
    @PostMapping("/invites/{inviteId}/response")
    public ApiResponse<RespondGroupInviteResponse> respondGroupInvite(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long inviteId,
            @Valid @RequestBody RespondGroupInviteRequest request
    ) {
        RespondGroupInviteCommand command = groupMapper.toRespondGroupInviteCommand(inviteId, request);
        RespondGroupInviteResult result = groupInviteService.respondGroupInvite(memberId, command);

        return ApiResponse.success(groupMapper.toRespondGroupInviteResponse(result));
    }

    @Override
    @PatchMapping("/{groupId}")
    public ApiResponse<UpdateGroupResponse> updateGroup(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupRequest request
    ) {
        UpdateGroupCommand command = groupMapper.toUpdateGroupCommand(groupId, request);
        UpdateGroupResult result = groupManagementService.updateGroup(memberId, command);

        return ApiResponse.success(groupMapper.toUpdateGroupResponse(result));
    }

    @Override
    @PostMapping("/join")
    public ApiResponse<JoinGroupResponse> joinGroup(
            @AuthenticatedMemberId Long memberId,
            @Valid @RequestBody JoinGroupRequest request
    ) {
        JoinGroupCommand command = groupMapper.toJoinGroupCommand(request);
        JoinGroupResult result = groupInviteService.joinGroup(memberId, command);

        return ApiResponse.success(groupMapper.toJoinGroupResponse(result));
    }

    @Override
    @PostMapping("/invite-links/join")
    public ApiResponse<JoinGroupResponse> joinGroupByInviteLink(
            @AuthenticatedMemberId Long memberId,
            @Valid @RequestBody JoinGroupByInviteLinkRequest request
    ) {
        JoinGroupResult result = groupInviteService.joinGroupByInviteLink(memberId, request.token());
        return ApiResponse.success(groupMapper.toJoinGroupResponse(result));
    }

    @Override
    @PostMapping("/{groupId}/leave")
    public ApiResponse<LeaveGroupResponse> leaveGroup(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId
    ) {
        LeaveGroupCommand command = groupMapper.toLeaveGroupCommand(groupId);
        LeaveGroupResult result = groupManagementService.leaveGroup(memberId, command);

        return ApiResponse.success(groupMapper.toLeaveGroupResponse(result));
    }

    @Override
    @DeleteMapping("/{groupId}")
    public ApiResponse<DeleteGroupResponse> deleteGroup(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId
    ) {
        DeleteGroupCommand command = groupMapper.toDeleteGroupCommand(groupId);
        DeleteGroupResult result = groupManagementService.deleteGroup(memberId, command);

        return ApiResponse.success(groupMapper.toDeleteGroupResponse(result));
    }

    @Override
    @PostMapping("/{groupId}/recommendations")
    public ApiResponse<CreateGroupRecommendationResponse> createRecommendation(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId,
            @Valid @RequestBody CreateGroupRecommendationRequest request
    ) {
        CreateGroupRecommendationCommand command = groupMapper.toCreateGroupRecommendationCommand(groupId, request);
        CreateGroupRecommendationResult result = groupRecommendationService.createGroupRecommendation(memberId, command);

        return ApiResponse.success(groupMapper.toCreateGroupRecommendationResponse(result));
    }

    @Override
    @GetMapping("/{groupId}/recommendations")
    public ApiResponse<PageResponse<GroupRecommendationSummaryResponse>> getRecommendations(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId,
            @Min(0) @RequestParam(defaultValue = "0")
            Integer page,

            @Min(1) @Max(100) @RequestParam(defaultValue = "20")
            Integer size
    ) {
        Page<GroupRecommendationSummaryResult> results =
                groupRecommendationService.getGroupRecommendations(memberId, groupId, page, size);
        PageResponse<GroupRecommendationSummaryResponse> response =
                PageResponse.of(results, groupMapper::toGroupRecommendationSummaryResponse);

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/{groupId}/recommendations/{sessionId}")
    public ApiResponse<GroupRecommendationDetailResponse> getRecommendation(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId,
            @PathVariable Long sessionId
    ) {
        GroupRecommendationDetailResult result = groupRecommendationService.getGroupRecommendation(memberId, groupId, sessionId);

        return ApiResponse.success(groupMapper.toGroupRecommendationDetailResponse(result));
    }

    @Override
    @GetMapping("/{groupId}/recommendations/{sessionId}/candidates")
    public ApiResponse<GroupRecommendationCandidateListResponse> getRecommendationCandidates(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId,
            @PathVariable Long sessionId
    ) {
        GroupRecommendationCandidateListResult result =
                groupRecommendationService.getGroupRecommendationCandidates(memberId, groupId, sessionId);

        return ApiResponse.success(groupMapper.toGroupRecommendationCandidateListResponse(result));
    }

    @Override
    @GetMapping("/{groupId}/recommendations/{sessionId}/readiness")
    public ApiResponse<GroupRecommendationReadinessResponse> getRecommendationReadiness(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId,
            @PathVariable Long sessionId
    ) {
        GroupRecommendationReadinessResult result =
                groupRecommendationService.getGroupRecommendationReadiness(memberId, groupId, sessionId);

        return ApiResponse.success(groupMapper.toGroupRecommendationReadinessResponse(result));
    }

    @Override
    @PostMapping("/{groupId}/recommendations/{sessionId}/ready")
    public ApiResponse<ReadyGroupRecommendationResponse> readyRecommendation(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId,
            @PathVariable Long sessionId
    ) {
        ReadyGroupRecommendationResult result = groupRecommendationService.readyGroupRecommendation(memberId, groupId, sessionId);

        return ApiResponse.success(groupMapper.toReadyGroupRecommendationResponse(result));
    }

    @Override
    @Deprecated
    @PostMapping("/{groupId}/recommendations/{sessionId}/reroll")
    public ApiResponse<CreateGroupRecommendationResponse> rerollRecommendation(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId,
            @PathVariable Long sessionId,
            @Valid @RequestBody RerollGroupRecommendationRequest request
    ) {
        CreateGroupRecommendationResult result = groupRecommendationService.rerollGroupRecommendation(
                memberId,
                groupId,
                sessionId,
                request.rerollType(),
                null
        );

        return ApiResponse.success(groupMapper.toCreateGroupRecommendationResponse(result));
    }

    @Override
    @PostMapping("/{groupId}/recommendations/{sessionId}/votes")
    public ApiResponse<GroupVoteResponse> vote(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId,
            @PathVariable Long sessionId,
            @Valid @RequestBody VoteGroupRecommendationRequest request
    ) {
        GroupVoteResult result =
                groupRecommendationService.voteGroupRecommendation(memberId, groupId, sessionId, request.candidateId());
        GroupVoteResponse response = groupMapper.toGroupVoteResponse(result);

        return ApiResponse.success(response);
    }

    @Override
    @PatchMapping("/{groupId}/recommendations/{sessionId}/finalize")
    public ApiResponse<FinalizeGroupRecommendationResponse> finalizeRecommendation(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId,
            @PathVariable Long sessionId,
            @Valid @RequestBody(required = false) FinalizeGroupRecommendationRequest request
    ) {
        FinalizeGroupRecommendationCommand command = groupMapper.toFinalizeGroupRecommendationCommand(groupId, sessionId, request);
        FinalizeGroupRecommendationResult result = groupRecommendationService.finalizeGroupRecommendation(memberId, command);
        return ApiResponse.success(groupMapper.toFinalizeGroupRecommendationResponse(result));
    }
}
