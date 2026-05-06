package matchuri.backend.api.group;

import jakarta.validation.Valid;
import java.util.List;
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
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.api.PageResponse;
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
public class GroupController implements GroupApi {

    @Override
    @PostMapping
    public ApiResponse<CreateGroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        return ApiResponse.success(CreateGroupResponse.mockActive());
    }

    @Override
    @GetMapping
    public ApiResponse<PageResponse<GroupSummaryResponse>> getMyGroups(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return ApiResponse.success(PageResponse.mock(List.of(GroupSummaryResponse.mockActive())));
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
    @PostMapping("/{groupId}/recommendation-sessions")
    public ApiResponse<CreateGroupRecommendationResponse> createRecommendation(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateGroupRecommendationRequest request
    ) {
        return ApiResponse.success(CreateGroupRecommendationResponse.mockOpen());
    }

    @Override
    @GetMapping("/{groupId}/recommendation-sessions/{sessionId}")
    public ApiResponse<GroupRecommendationSessionResponse> getRecommendation(
            @PathVariable Long groupId,
            @PathVariable Long sessionId
    ) {
        return ApiResponse.success(GroupRecommendationSessionResponse.mockOpen());
    }

    @Override
    @GetMapping("/{groupId}/recommendation-sessions/{sessionId}/candidates")
    public ApiResponse<GroupRecommendationCandidateListResponse> getRecommendationCandidates(
            @PathVariable Long groupId,
            @PathVariable Long sessionId
    ) {
        return ApiResponse.success(GroupRecommendationCandidateListResponse.mock());
    }

    @Override
    @PostMapping("/{groupId}/recommendation-sessions/{sessionId}/votes")
    public ApiResponse<GroupVoteResponse> vote(
            @PathVariable Long groupId,
            @PathVariable Long sessionId,
            @Valid @RequestBody VoteGroupRecommendationRequest request
    ) {
        return ApiResponse.success(GroupVoteResponse.mockVoted(request.candidateId(), request.voteValue()));
    }

    @Override
    @PatchMapping("/{groupId}/recommendation-sessions/{sessionId}/finalize")
    public ApiResponse<FinalizeGroupRecommendationResponse> finalizeRecommendation(
            @PathVariable Long groupId,
            @PathVariable Long sessionId
    ) {
        return ApiResponse.success(FinalizeGroupRecommendationResponse.mockFinalized());
    }
}
