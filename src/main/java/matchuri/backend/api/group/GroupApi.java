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
import matchuri.backend.api.group.dto.docs.CreateNicknameGroupInviteApiResponse;
import matchuri.backend.api.group.dto.docs.CreateGroupRecommendationApiResponse;
import matchuri.backend.api.group.dto.docs.DeleteGroupApiResponse;
import matchuri.backend.api.group.dto.docs.FinalizeGroupRecommendationApiResponse;
import matchuri.backend.api.group.dto.docs.GroupApiExamples;
import matchuri.backend.api.group.dto.docs.GroupDetailApiResponse;
import matchuri.backend.api.group.dto.docs.GroupInviteSummaryPageApiResponse;
import matchuri.backend.api.group.dto.docs.GroupRecommendationCandidateListApiResponse;
import matchuri.backend.api.group.dto.docs.GroupRecommendationSessionApiResponse;
import matchuri.backend.api.group.dto.docs.GroupRecommendationSummaryPageApiResponse;
import matchuri.backend.api.group.dto.docs.GroupSummaryPageApiResponse;
import matchuri.backend.api.group.dto.docs.GroupVoteApiResponse;
import matchuri.backend.api.group.dto.docs.JoinGroupApiResponse;
import matchuri.backend.api.group.dto.docs.LeaveGroupApiResponse;
import matchuri.backend.api.group.dto.docs.RespondGroupInviteApiResponse;
import matchuri.backend.api.group.dto.docs.UpdateGroupApiResponse;
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
import matchuri.backend.api.group.dto.response.GroupRecommendationSummaryResponse;
import matchuri.backend.api.group.dto.response.GroupSummaryResponse;
import matchuri.backend.api.group.dto.response.GroupVoteResponse;
import matchuri.backend.api.group.dto.response.JoinGroupResponse;
import matchuri.backend.api.group.dto.response.LeaveGroupResponse;
import matchuri.backend.api.group.dto.response.RespondGroupInviteResponse;
import matchuri.backend.api.group.dto.response.UpdateGroupResponse;
import matchuri.backend.domain.group.entity.GroupInviteStatus;
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
                    - 그룹마다 하나의 고정 초대 코드를 생성합니다.
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
            summary = "그룹 상세 조회",
            description = """
                    그룹 방 상세와 현재 멤버, 진행 중인 그룹 추천 상태를 조회합니다.

                    - 로그인한 활성 회원만 사용할 수 있습니다.
                    - 현재 회원이 해당 그룹의 `ACTIVE` 멤버일 때만 조회할 수 있습니다.
                    - 그룹의 모든 `ACTIVE` 멤버에게 고정 초대 코드를 함께 반환합니다.
                    - 삭제된 그룹은 조회할 수 없습니다.
                    - 열린 그룹 추천이 있으면 `activeRecommendation`을 함께 반환합니다.
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
            summary = "닉네임 기반 그룹 초대 생성",
            description = """
                    닉네임으로 특정 회원에게 그룹 초대 요청을 보냅니다.

                    구현 기준:
                    - 로그인한 활성 회원만 사용할 수 있습니다.
                    - 현재 회원이 해당 그룹의 `ACTIVE` OWNER 멤버일 때만 초대할 수 있습니다.
                    - 초대 대상은 활성 회원의 nickname으로 찾습니다.
                    - 자기 자신, 이미 그룹에 참여 중인 회원, 동일 그룹/대상의 `PENDING` 초대는 거절합니다.
                    - 초대 요청은 생성 시점부터 24시간 뒤 만료됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "초대 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateNicknameGroupInviteApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.CREATE_NICKNAME_INVITE_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<CreateNicknameGroupInviteResponse> createNicknameInvite(
            @Valid CreateNicknameGroupInviteRequest request
    );

    @Operation(
            summary = "내 그룹 초대 목록 조회",
            description = """
                    현재 회원이 받은 그룹 초대 목록을 조회합니다.

                    - 로그인한 활성 회원만 사용할 수 있습니다.
                    - 현재 회원이 초대 대상인 초대 요청만 반환합니다.
                    - `status`를 생략하면 `PENDING` 초대만 조회합니다.
                    - 생성 시각 최신순으로 정렬합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GroupInviteSummaryPageApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.MY_INVITES_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<PageResponse<GroupInviteSummaryResponse>> getMyInvites(
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

    @Operation(
            summary = "그룹 초대 응답",
            description = """
                    현재 회원이 받은 nickname 기반 그룹 초대에 응답합니다.

                    구현 기준:
                    - 로그인한 활성 회원만 사용할 수 있습니다.
                    - 현재 회원이 해당 초대의 대상 회원일 때만 응답할 수 있습니다.
                    - `PENDING` 상태이고 만료되지 않은 초대만 응답할 수 있습니다.
                    - `ACCEPT`이면 그룹 membership을 생성하거나 기존 `LEFT` membership을 재활성화합니다.
                    - `DECLINE`이면 초대 상태만 `DECLINED`로 닫고 membership은 변경하지 않습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "초대 응답 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RespondGroupInviteApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.RESPOND_INVITE_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<RespondGroupInviteResponse> respondGroupInvite(
            Long inviteId,
            @Valid RespondGroupInviteRequest request
    );

    @Operation(
            summary = "그룹 수정",
            description = """
                    그룹 정보를 수정합니다.

                    구현 기준:
                    - 현재 MVP에서는 그룹 이름과 위치(위도/경도)를 수정합니다.
                    - 생략된 필드는 변경하지 않습니다.
                    - 요청에는 최소 1개 이상의 지원 필드가 포함되어야 합니다.
                    - 현재 회원이 해당 그룹의 `ACTIVE` OWNER 멤버일 때만 수정할 수 있습니다.
                    - 그룹이 `ACTIVE` 상태일 때만 수정할 수 있습니다.
                    - 수정 시점에 열린 그룹 추천이 있으면 `openGroupRecommendationId`를 함께 반환합니다.
                    - `openGroupRecommendationId`가 있으면 위치 변경 등을 반영하기 위해 해당 추천 ID로 `INPUT_CHANGED` 재요청을 이어갈 수 있습니다.
                    - 진행 중인 그룹 추천이 없으면 `openGroupRecommendationId`는 `null`입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "그룹 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateGroupApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.UPDATE_GROUP_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<UpdateGroupResponse> updateGroup(
            Long groupId,
            @Valid UpdateGroupRequest request
    );

    @Operation(
            summary = "초대 코드로 그룹 참여",
            description = """
                    초대 코드로 그룹에 참여합니다.

                    구현 기준:
                    - 그룹의 고정 초대 코드 존재 여부를 검증합니다.
                    - 연결된 그룹이 `ACTIVE` 상태일 때만 참여할 수 있습니다.
                    - 이미 `ACTIVE` 멤버이면 중복 참여로 실패합니다.
                    - 과거 `LEFT` 멤버는 기존 membership을 재활성화합니다.
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
            summary = "그룹 탈퇴",
            description = """
                    현재 회원이 그룹에서 탈퇴합니다.

                    구현 기준:
                    - 현재 회원의 활성 membership을 `LEFT`로 전환합니다.
                    - `OWNER`는 나갈 수 없으며 그룹 삭제 API를 사용해야 합니다.
                    - 이미 나간 멤버와 비멤버 접근은 상태에 맞는 에러로 처리합니다.
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
            summary = "그룹 삭제",
            description = """
                    그룹을 삭제 상태로 전환합니다.

                    구현 기준:
                    - 현재 회원이 해당 그룹의 `ACTIVE` OWNER 멤버일 때만 삭제할 수 있습니다.
                    - 그룹은 `DELETED` 상태로 전환됩니다.
                    - 해당 그룹의 `PENDING` 초대 요청은 `REVOKED`로 전환됩니다.
                    - 해당 그룹의 `ACTIVE` 멤버는 후속 조회에서 노출되지 않도록 `LEFT`로 전환됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "그룹 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeleteGroupApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.DELETE_GROUP_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<DeleteGroupResponse> deleteGroup(Long groupId);

    @Operation(
            summary = "그룹 추천 시작",
            description = """
                    그룹 추천을 시작하고 후보 메뉴를 생성합니다.

                    - 로그인한 활성 회원만 사용할 수 있습니다.
                    - MVP에서는 해당 그룹의 `OWNER`만 시작할 수 있습니다.
                    - 한 그룹에는 동시에 `OPEN` 그룹 추천을 1개만 허용합니다.
                    - 그룹 활성 멤버의 취향 프로필을 모아 후보를 생성합니다.
                    - 취향 프로필이 없는 멤버는 빈 취향으로 반영합니다.
                    - 생성된 후보에는 추천 점수와 내부 메타데이터를 저장합니다.
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
            summary = "그룹 추천 요청 리스트 조회",
            description = """
                    특정 그룹 방에서 생성된 그룹 추천 요청 목록을 조회합니다.

                    구현 기준:
                    - 로그인한 활성 회원만 사용할 수 있습니다.
                    - 현재 회원이 해당 그룹의 `ACTIVE` 멤버일 때만 조회할 수 있습니다.
                    - 삭제된 그룹은 조회할 수 없습니다.
                    - 응답은 `sessionId`, `status`, `startedAt`, `endedAt`만 포함하는 얇은 summary입니다.
                    - `finalCandidate`, `finalMenuName`, `voteProgress`, `status` 필터는 1차 범위에서 제외합니다.
                    - 최신순(`startedAt DESC`, `id DESC`)으로 정렬합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GroupRecommendationSummaryPageApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = GroupApiExamples.RECOMMENDATION_LIST_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<PageResponse<GroupRecommendationSummaryResponse>> getRecommendations(
            Long groupId,

            @Parameter(description = "0부터 시작하는 페이지 번호입니다.", example = "0")
            @Min(0)
            Integer page,

            @Parameter(description = "페이지 크기입니다. 기본값은 20입니다.", example = "20")
            @Min(1)
            @Max(100)
            Integer size
    );

    @Operation(
            summary = "그룹 추천 세션 상세 조회",
            description = """
                    그룹 추천 상태, 후보, 투표 진행률, 최종 후보를 조회합니다.

                    구현 기준:
                    - 로그인한 활성 회원만 사용할 수 있습니다.
                    - 현재 회원이 해당 그룹의 `ACTIVE` 멤버일 때만 조회할 수 있습니다.
                    - `sessionId`는 API 표현 이름이며 저장 모델은 `group_recommendations.id`로 해석합니다.
                    - 후보별 현재 투표 수와 전체 투표 진행률을 함께 반환합니다.
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
            summary = "그룹 추천 후보 목록 조회",
            description = """
                    그룹 추천 후보 메뉴 목록만 조회합니다.

                    구현 기준:
                    - 로그인한 활성 회원만 사용할 수 있습니다.
                    - 현재 회원이 해당 그룹의 `ACTIVE` 멤버일 때만 조회할 수 있습니다.
                    - 후보는 추천 순위 오름차순으로 반환합니다.
                    - 후보별 현재 투표 수를 함께 반환합니다.
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
            summary = "그룹 추천 재요청",
            description = """
                    열린 그룹 추천을 종료하고 새 그룹 추천을 생성합니다.

                    구현 기준:
                    - 로그인한 활성 회원만 사용할 수 있습니다.
                    - MVP에서는 해당 그룹의 `ACTIVE` OWNER 멤버만 재요청할 수 있습니다.
                    - source 그룹 추천은 해당 그룹에 속하고 `OPEN` 상태여야 합니다.
                    - `NOT_SATISFIED`는 source 후보 전체를 `group_menu_actions.SKIP`으로 저장한 뒤 source를 `REROLLED_WITH_SKIP`으로 종료합니다.
                    - `INPUT_CHANGED`는 `SKIP` 로그 없이 source를 `REROLLED_WITHOUT_SKIP`으로 종료합니다.
                    - 새 후보 계산에서는 같은 그룹의 최근 24시간 `SKIP` 메뉴를 제외합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "그룹 추천 재요청 성공",
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
    ApiResponse<CreateGroupRecommendationResponse> rerollRecommendation(
            Long groupId,
            Long sessionId,
            @Valid RerollGroupRecommendationRequest request
    );

    @Operation(
            summary = "그룹 추천 후보 투표",
            description = """
                    그룹 추천 후보에 투표합니다.

                    정책:
                    - 활성 그룹 멤버만 투표할 수 있습니다.
                    - 열린 그룹 추천(`OPEN`)에만 투표할 수 있습니다.
                    - 회원은 추천 세션당 한 번만 투표할 수 있습니다.
                    - `voteValue`는 사용하지 않고 후보 선택 여부만 저장합니다.
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
            summary = "그룹 추천 최종 메뉴 확정",
            description = """
                    투표 결과를 바탕으로 그룹의 최종 메뉴를 확정합니다.

                    정책:
                    - 해당 그룹의 `ACTIVE` `OWNER` 멤버만 최종 확정할 수 있습니다.
                    - 최다 득표 후보를 최종 후보로 저장합니다.
                    - 동률이면 추천 순위 `rankNo`가 가장 낮은 후보를 선택합니다.
                    - 투표가 0건이면 `rankNo=1` 후보를 선택합니다.
                    - 확정 후 그룹 추천 상태는 `FINALIZED`가 됩니다.
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
