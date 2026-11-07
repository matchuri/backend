package matchuri.backend.api.group;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.group.dto.response.GroupDetailV2Response;
import matchuri.backend.api.group.dto.response.GroupRecommendationV2SummaryResponse;
import matchuri.backend.api.group.mapper.GroupMapper;
import matchuri.backend.domain.group.result.GroupDetailResult;
import matchuri.backend.domain.group.result.GroupRecommendationV2SummaryResult;
import matchuri.backend.domain.group.service.GroupManagementService;
import matchuri.backend.domain.group.service.GroupRecommendationService;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.api.PageResponse;
import matchuri.backend.global.security.AuthenticatedMemberId;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/groups")
@Validated
@RequiredArgsConstructor
public class GroupV2Controller implements GroupV2Api {

    private final GroupManagementService groupManagementService;
    private final GroupRecommendationService groupRecommendationService;
    private final GroupMapper groupMapper;

    @Override
    @GetMapping("/{groupId}")
    public ApiResponse<GroupDetailV2Response> getGroup(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId
    ) {
        GroupDetailResult result = groupManagementService.getGroupV2(memberId, groupId);

        return ApiResponse.success(groupMapper.toGroupDetailV2Response(result));
    }

    @Override
    @GetMapping("/{groupId}/recommendations")
    public ApiResponse<PageResponse<GroupRecommendationV2SummaryResponse>> getRecommendations(
            @AuthenticatedMemberId Long memberId,
            @PathVariable Long groupId,
            @Min(0) @RequestParam(defaultValue = "0") Integer page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "20") Integer size
    ) {
        Page<@NonNull GroupRecommendationV2SummaryResult> results =
                groupRecommendationService.getGroupRecommendationsV2(memberId, groupId, page, size);
        PageResponse<GroupRecommendationV2SummaryResponse> response =
                PageResponse.of(results, groupMapper::toGroupRecommendationV2SummaryResponse);

        return ApiResponse.success(response);
    }
}
