package matchuri.backend.api.group;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import matchuri.backend.api.group.dto.docs.GroupInviteExistsApiResponse;
import matchuri.backend.api.group.dto.response.GroupInviteExistsResponse;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.security.AuthenticatedMemberId;

public interface GroupInviteApi {

    @Operation(
            summary = "내 PENDING 그룹 초대 존재 여부 조회",
            description = """
                    현재 로그인한 회원에게 아직 유효한 PENDING 그룹 초대가 존재하는지 반환합니다.

                    상태가 PENDING이어도 expiresAt이 현재 시각 이하이면 존재 여부에서 제외합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GroupInviteExistsApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "exists": true
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<GroupInviteExistsResponse> checkMyInviteExists(
            @AuthenticatedMemberId Long memberId
    );
}
