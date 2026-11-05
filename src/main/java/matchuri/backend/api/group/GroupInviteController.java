package matchuri.backend.api.group;

import lombok.RequiredArgsConstructor;
import matchuri.backend.api.group.dto.response.GroupInviteExistsResponse;
import matchuri.backend.domain.group.service.GroupInviteService;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.security.AuthenticatedMemberId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invites")
@RequiredArgsConstructor
public class GroupInviteController implements GroupInviteApi {

    private final GroupInviteService groupInviteService;

    @Override
    @GetMapping("/me/exists")
    public ApiResponse<GroupInviteExistsResponse> checkMyInviteExists(
            @AuthenticatedMemberId Long memberId
    ) {
        return ApiResponse.success(new GroupInviteExistsResponse(
                groupInviteService.existsMyPendingInvite(memberId)
        ));
    }
}
