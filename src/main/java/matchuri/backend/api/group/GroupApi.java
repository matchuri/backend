package matchuri.backend.api.group;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import matchuri.backend.api.group.dto.docs.CreateGroupApiResponse;
import matchuri.backend.api.group.dto.docs.CreateGroupInviteApiResponse;
import matchuri.backend.api.group.dto.docs.CreateGroupRecommendationApiResponse;
import matchuri.backend.api.group.dto.docs.FinalizeGroupRecommendationApiResponse;
import matchuri.backend.api.group.dto.docs.GroupApiExamples;
import matchuri.backend.api.group.dto.docs.GroupDetailApiResponse;
import matchuri.backend.api.group.dto.docs.GroupRecommendationCandidateListApiResponse;
import matchuri.backend.api.group.dto.docs.GroupRecommendationSessionApiResponse;
import matchuri.backend.api.group.dto.docs.GroupSummaryPageApiResponse;
import matchuri.backend.api.group.dto.docs.GroupVoteApiResponse;
import matchuri.backend.api.group.dto.docs.JoinGroupApiResponse;
import matchuri.backend.api.group.dto.docs.LeaveGroupApiResponse;
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
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.api.PageResponse;

@Tag(name = "Group Decision", description = "그룹 메뉴 의사결정 API")
public interface GroupApi {

    @Operation(
            summary = "그룹 생성",
            description = """
                    그룹 방을 생성합니다.

                    - 로그인한 활성 회원만 사용할 수 있습니다.
                    - 생성자는 `OWNER` 멤버로 함께 저장됩니다.
                    - 실제 저장 테이블은 `group_rooms`, `group_room_members`입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "그룹 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateGroupApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.CREATE_GROUP_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<CreateGroupResponse> createGroup(@Valid CreateGroupRequest request);

    @Operation(
            summary = "내 그룹 목록 조회",
            description = """
                    내가 속한 그룹 목록을 조회합니다.

                    - 로그인한 활성 회원만 사용할 수 있습니다.
                    - 현재 회원이 `ACTIVE` 멤버로 속한 그룹만 반환합니다.
                    - 삭제된 그룹은 목록에서 제외합니다.
                    - 그룹 추천 구현 전까지 `latestRecommendationStatus`는 null일 수 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GroupSummaryPageApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.GROUP_LIST_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<PageResponse<GroupSummaryResponse>> getMyGroups(
            @Parameter(description = "그룹 상태 필터입니다. 생략하면 전체 상태를 조회합니다.", example = "ACTIVE")
            GroupRoomStatus status,

            @Parameter(description = "0부터 시작하는 페이지 번호입니다.", example = "0")
            @Min(0)
            Integer page,

            @Parameter(description = "페이지 크기입니다. 기본값은 20입니다.", example = "20")
            @Min(1)
            @Max(100)
            Integer size
    );

    @Operation(
            summary = "그룹 상세 조회 (Mock API)",
            description = """
                    그룹 방 상세와 현재 멤버, 진행 중인 그룹 추천 상태를 조회합니다.

                    Mock API 상태:
                    - `groupId` 존재 여부와 멤버 권한 검증은 아직 수행하지 않습니다.
                    - 최신 스키마의 그룹 추천 저장 책임은 `group_recommendations` 기준입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GroupDetailApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.GROUP_DETAIL_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<GroupDetailResponse> getGroup(Long groupId);

    @Operation(
            summary = "그룹 초대 코드 생성 (Mock API)",
            description = """
                    그룹에 참여할 수 있는 초대 코드를 생성합니다.

                    Mock API 상태:
                    - 그룹 멤버 권한과 만료 정책 저장은 아직 수행하지 않습니다.
                    - 실제 저장 테이블은 `group_invites` 기준입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "초대 코드 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateGroupInviteApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.CREATE_INVITE_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<CreateGroupInviteResponse> createInvite(Long groupId);

    @Operation(
            summary = "초대 코드로 그룹 참여 (Mock API)",
            description = """
                    초대 코드로 그룹에 참여합니다.

                    Mock API 상태:
                    - 초대 코드 존재 여부, 만료 여부, 중복 가입 검증은 아직 수행하지 않습니다.
                    - request body validation만 수행합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "그룹 참여 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JoinGroupApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.JOIN_GROUP_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<JoinGroupResponse> joinGroup(@Valid JoinGroupRequest request);

    @Operation(
            summary = "그룹 탈퇴 (Mock API)",
            description = """
                    현재 회원이 그룹에서 탈퇴합니다.

                    Mock API 상태:
                    - 그룹 멤버 여부와 소유자 탈퇴 정책은 아직 검증하지 않습니다.
                    - 실제 저장 테이블은 `group_room_members` 기준입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "그룹 탈퇴 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LeaveGroupApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.LEAVE_GROUP_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<LeaveGroupResponse> leaveGroup(Long groupId);

    @Operation(
            summary = "그룹 추천 시작 (Mock API)",
            description = """
                    그룹 추천을 시작하고 후보 메뉴를 생성합니다.

                    Mock API 상태:
                    - request body validation만 수행합니다.
                    - 그룹 취향 집계와 후보 생성 알고리즘은 아직 수행하지 않습니다.
                    - API 경로는 사용자 흐름상 `recommendations`를 사용하며, 최신 저장 테이블은 `group_recommendations` 기준입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "그룹 추천 시작 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateGroupRecommendationApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.CREATE_RECOMMENDATION_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<CreateGroupRecommendationResponse> createRecommendation(
            Long groupId,
            @Valid CreateGroupRecommendationRequest request
    );

    @Operation(
            summary = "그룹 추천 세션 상세 조회 (Mock API)",
            description = """
                    그룹 추천 상태, 후보, 투표 진행률, 최종 후보를 조회합니다.

                    Mock API 상태:
                    - `groupId`, `sessionId` 존재 여부와 접근 권한은 아직 검증하지 않습니다.
                    - `sessionId`는 API 표현 이름이며 저장 모델은 `group_recommendations.id`로 해석합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GroupRecommendationSessionApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.RECOMMENDATION_SESSION_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<GroupRecommendationSessionResponse> getRecommendation(Long groupId, Long sessionId);

    @Operation(
            summary = "그룹 추천 후보 목록 조회 (Mock API)",
            description = """
                    그룹 추천 후보 메뉴 목록만 조회합니다.

                    Mock API 상태:
                    - 후보 3개와 현재 투표수를 고정 응답으로 반환합니다.
                    - 실제 저장 테이블은 `group_recommendation_candidates` 기준입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GroupRecommendationCandidateListApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.RECOMMENDATION_CANDIDATES_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<GroupRecommendationCandidateListResponse> getRecommendationCandidates(Long groupId, Long sessionId);

    @Operation(
            summary = "그룹 추천 후보 투표 (Mock API)",
            description = """
                    그룹 추천 후보에 투표합니다.

                    Mock API 상태:
                    - request body validation만 수행합니다.
                    - 중복 투표, 후보 소속, 세션 상태 검증은 아직 수행하지 않습니다.
                    - 실제 저장 테이블은 `group_recommendation_votes` 기준입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "투표 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GroupVoteApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.VOTE_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<GroupVoteResponse> vote(Long groupId, Long sessionId, @Valid VoteGroupRecommendationRequest request);

    @Operation(
            summary = "그룹 추천 최종 메뉴 확정 (Mock API)",
            description = """
                    투표 결과를 바탕으로 그룹의 최종 메뉴를 확정합니다.

                    Mock API 상태:
                    - 실제 투표 집계와 동률 처리 정책은 아직 수행하지 않습니다.
                    - 항상 1순위 후보를 최종 후보로 반환합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "확정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FinalizeGroupRecommendationApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.FINALIZE_RECOMMENDATION_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<FinalizeGroupRecommendationResponse> finalizeRecommendation(Long groupId, Long sessionId);
}
