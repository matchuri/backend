package matchuri.backend.api.group;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import matchuri.backend.api.group.dto.docs.GroupApiExamples;
import matchuri.backend.api.group.dto.docs.GroupDetailV2ApiResponse;
import matchuri.backend.api.group.dto.docs.GroupRecommendationV2SummaryPageApiResponse;
import matchuri.backend.api.group.dto.response.GroupDetailV2Response;
import matchuri.backend.api.group.dto.response.GroupRecommendationV2SummaryResponse;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.api.PageResponse;
import matchuri.backend.global.security.AuthenticatedMemberId;

public interface GroupV2Api {

    @Operation(
            summary = "그룹 상세 조회 v2",
            description = """
                    그룹 방 상세와 현재 멤버, 가장 최근 그룹 추천 상태를 조회합니다.

                    - 로그인한 활성 회원만 사용할 수 있습니다.
                    - 현재 회원이 해당 그룹의 `ACTIVE` 멤버일 때만 조회할 수 있습니다.
                    - 그룹의 모든 `ACTIVE` 멤버에게 고정 초대 코드를 함께 반환합니다.
                    - 멤버 목록에는 각 회원의 `memberProfileImageUrl`이 포함되며, 이미지가 없으면 null입니다.
                    - 멤버 목록의 각 항목은 현재 로그인한 회원이면 `isMe=true`, 아니면 `false`를 반환합니다.
                    - 삭제된 그룹은 조회할 수 없습니다.
                    - 가장 최근 추천 세션이 있으면 `recentlyRecommendation`을 함께 반환합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GroupDetailV2ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.GROUP_DETAIL_V2_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<GroupDetailV2Response> getGroup(@AuthenticatedMemberId Long memberId, Long groupId);

    @Operation(
            summary = "그룹 추천 요청 리스트 조회 v2",
            description = """
                    특정 그룹 방에서 생성된 그룹 추천 요청 목록을 최종 선정 메뉴명과 함께 조회합니다.

                    - 로그인한 활성 회원만 사용할 수 있습니다.
                    - 현재 회원이 해당 그룹의 `ACTIVE` 멤버일 때만 조회할 수 있습니다.
                    - 삭제된 그룹은 조회할 수 없습니다.
                    - `PREPARING`, `OPEN`, 종료 상태 세션을 모두 포함합니다.
                    - `selectedMenuName`은 `FINALIZED`이고 최종 후보가 있을 때만 메뉴명이며, 그 외에는 null입니다.
                    - 그룹 추천 결과 기록 목록에는 이 API를 사용합니다.
                    - 후보와 투표 등 개별 기록 상세는 그룹 추천 세션 상세 조회 API를 사용합니다.
                    - 최신순(`createdAt DESC`, `id DESC`)으로 정렬합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GroupRecommendationV2SummaryPageApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.RECOMMENDATION_LIST_V2_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<PageResponse<GroupRecommendationV2SummaryResponse>> getRecommendations(
            @AuthenticatedMemberId Long memberId,
            Long groupId,

            @Parameter(description = "0부터 시작하는 페이지 번호입니다.", example = "0")
            @Min(0)
            Integer page,

            @Parameter(description = "페이지 크기입니다. 기본값은 20입니다.", example = "20")
            @Min(1)
            @Max(100)
            Integer size
    );
}
