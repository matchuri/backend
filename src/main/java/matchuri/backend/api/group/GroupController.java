package matchuri.backend.api.group;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.group.dto.request.CreateGroupRecommendationRequest;
import matchuri.backend.api.group.dto.request.CreateGroupRequest;
import matchuri.backend.api.group.dto.request.JoinGroupRequest;
import matchuri.backend.api.group.dto.request.VoteGroupRecommendationRequest;
import matchuri.backend.api.group.dto.response.CreateGroupInviteResponse;
import matchuri.backend.api.group.dto.response.CreateGroupRecommendationResponse;
import matchuri.backend.api.group.dto.response.CreateGroupResponse;
import matchuri.backend.api.group.dto.response.FinalizeGroupRecommendationResponse;
import matchuri.backend.api.group.dto.response.GroupDetailResponse;
import matchuri.backend.api.group.dto.response.GroupRecommendationCandidateListResponse;
import matchuri.backend.api.group.dto.response.GroupRecommendationSessionResponse;
import matchuri.backend.api.group.dto.response.GroupSummaryResponse;
import matchuri.backend.api.group.dto.response.GroupVoteResponse;
import matchuri.backend.api.group.dto.response.JoinGroupResponse;
import matchuri.backend.api.group.dto.response.LeaveGroupResponse;
import matchuri.backend.domain.group.command.CreateGroupCommand;
import matchuri.backend.domain.group.command.GetMyGroupsCommand;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.result.CreateGroupResult;
import matchuri.backend.domain.group.result.GroupSummaryResult;
import matchuri.backend.domain.group.service.GroupService;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.api.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
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
        Page<GroupSummaryResult> results = groupService.getMyGroups(command);
        PageResponse<GroupSummaryResponse> response = PageResponse.of(results, groupMapper::toGroupSummaryResponse);

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/{groupId}")
    public ApiResponse<GroupDetailResponse> getGroup(@PathVariable Long groupId) {
        return ApiResponse.success(GroupDetailResponse.mockActive());
    }

    @Override
    @PostMapping("/{groupId}/invites")
    public ApiResponse<CreateGroupInviteResponse> createInvite(@PathVariable Long groupId) {
        return ApiResponse.success(CreateGroupInviteResponse.mockActive());
    }

    @Override
    @PostMapping("/join")
    public ApiResponse<JoinGroupResponse> joinGroup(@Valid @RequestBody JoinGroupRequest request) {
        return ApiResponse.success(JoinGroupResponse.mockJoined());
    }

    @Override
    @PostMapping("/{groupId}/leave")
    public ApiResponse<LeaveGroupResponse> leaveGroup(@PathVariable Long groupId) {
        return ApiResponse.success(LeaveGroupResponse.mockLeft());
    }

    @Override
    @PostMapping("/{groupId}/recommendations")
    public ApiResponse<CreateGroupRecommendationResponse> createRecommendation(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateGroupRecommendationRequest request
    ) {
        return ApiResponse.success(CreateGroupRecommendationResponse.mockOpen());
    }

    @Override
    @GetMapping("/{groupId}/recommendations/{sessionId}")
    public ApiResponse<GroupRecommendationSessionResponse> getRecommendation(
            @PathVariable Long groupId,
            @PathVariable Long sessionId
    ) {
        return ApiResponse.success(GroupRecommendationSessionResponse.mockOpen());
    }

    @Override
    @GetMapping("/{groupId}/recommendations/{sessionId}/candidates")
    public ApiResponse<GroupRecommendationCandidateListResponse> getRecommendationCandidates(
            @PathVariable Long groupId,
            @PathVariable Long sessionId
    ) {
        return ApiResponse.success(GroupRecommendationCandidateListResponse.mock());
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
