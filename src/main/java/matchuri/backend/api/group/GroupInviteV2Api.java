package matchuri.backend.api.group;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import matchuri.backend.api.group.dto.docs.GroupInviteV2SummaryPageApiResponse;
import matchuri.backend.api.group.dto.response.GroupInviteV2SummaryResponse;
import matchuri.backend.domain.group.entity.GroupInviteStatus;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.api.PageResponse;
import matchuri.backend.global.security.AuthenticatedMemberId;

public interface GroupInviteV2Api {

    @Operation(
            summary = "내 그룹 초대 목록 조회 v2",
            description = """
                    현재 회원이 받은 그룹 초대 목록을 조회합니다.

                    - 로그인한 활성 회원만 사용할 수 있습니다.
                    - 현재 회원이 초대 대상인 만료되지 않은 초대만 반환합니다.
                    - status를 생략하면 PENDING 초대만 조회합니다.
                    - 생성 시각 최신순으로 정렬합니다.
                    - 각 항목은 초대 PK ID, 그룹명, 초대자 프로필 이미지 URL, 초대자 닉네임으로 구성됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GroupInviteV2SummaryPageApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "content": [
                                                  {
                                                    "id": 501,
                                                    "groupName": "맛집 탐방 모임",
                                                    "requestMemberProfileImageUrl": "https://asset.matchuri.com/preset-profile/v1-spaghetti.png",
                                                    "requestMemberNickname": "나는야 임영웅"
                                                  }
                                                ],
                                                "pageInfo": {
                                                  "page": 0,
                                                  "size": 20,
                                                  "totalElements": 1,
                                                  "totalPages": 1,
                                                  "first": true,
                                                  "last": true,
                                                  "hasNext": false,
                                                  "hasPrevious": false
                                                }
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<PageResponse<GroupInviteV2SummaryResponse>> getMyInvites(
            @AuthenticatedMemberId Long memberId,
            @Parameter(description = "초대 상태 필터입니다. 생략하면 PENDING 초대만 조회합니다.", example = "PENDING")
            GroupInviteStatus status,

            @Parameter(description = "0부터 시작하는 페이지 번호입니다.", example = "0")
            @Min(0)
            Integer page,

            @Parameter(description = "페이지 크기입니다. 기본값은 20입니다.", example = "20")
            @Min(1)
            @Max(100)
            Integer size
    );
}
