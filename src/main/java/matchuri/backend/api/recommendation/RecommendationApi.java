package matchuri.backend.api.recommendation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import matchuri.backend.api.recommendation.dto.docs.GuestPersonalRecommendationApiResponse;
import matchuri.backend.api.recommendation.dto.docs.PersonalRecommendationCandidateListApiResponse;
import matchuri.backend.api.recommendation.dto.docs.PersonalRecommendationDetailApiResponse;
import matchuri.backend.api.recommendation.dto.docs.PersonalRecommendationRequestApiResponse;
import matchuri.backend.api.recommendation.dto.docs.PersonalRecommendationSummaryPageApiResponse;
import matchuri.backend.api.recommendation.dto.docs.RecommendationApiExamples;
import matchuri.backend.api.recommendation.dto.docs.SelectPersonalRecommendationApiResponse;
import matchuri.backend.api.recommendation.dto.request.CreateGuestPersonalRecommendationRequest;
import matchuri.backend.api.recommendation.dto.request.CreatePersonalRecommendationRequest;
import matchuri.backend.api.recommendation.dto.request.SelectPersonalRecommendationRequest;
import matchuri.backend.api.recommendation.dto.response.GuestPersonalRecommendationResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationCandidateListResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationDetailResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationRequestResponse;
import matchuri.backend.api.recommendation.dto.response.PersonalRecommendationResponse;
import matchuri.backend.api.recommendation.dto.response.SelectPersonalRecommendationResponse;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.api.PageResponse;

@Tag(name = "Personal Recommendation", description = "개인 메뉴 추천 API")
public interface RecommendationApi {

    @Operation(
            summary = "비회원 개인 추천 요청",
            description = """
                    비회원이 입력한 취향 정보를 바탕으로 개인 메뉴 추천을 실행합니다.

                    추천 이력은 저장하지 않으며, `restriction ingredient`, `disliked menu item`을 제외한 후보만 반환합니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비회원 추천 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GuestPersonalRecommendationApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = RecommendationApiExamples.GUEST_PERSONAL_RECOMMENDATION_CREATE_SUCCESS
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "중복되었거나 유효하지 않은 취향 입력",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "duplicateAttributeCategory",
                                            value = RecommendationApiExamples.GUEST_RECOMMENDATION_DUPLICATE_ATTRIBUTE_CATEGORY
                                    ),
                                    @ExampleObject(
                                            name = "duplicateRestrictionIngredient",
                                            value = RecommendationApiExamples.GUEST_RECOMMENDATION_DUPLICATE_RESTRICTION_INGREDIENT
                                    ),
                                    @ExampleObject(
                                            name = "duplicateDislikedMenuItem",
                                            value = RecommendationApiExamples.GUEST_RECOMMENDATION_DUPLICATE_DISLIKED_MENU_ITEM
                                    ),
                                    @ExampleObject(
                                            name = "invalidAttributeCategory",
                                            value = RecommendationApiExamples.GUEST_RECOMMENDATION_INVALID_ATTRIBUTE_CATEGORY
                                    ),
                                    @ExampleObject(
                                            name = "invalidRestrictionIngredient",
                                            value = RecommendationApiExamples.GUEST_RECOMMENDATION_INVALID_RESTRICTION_INGREDIENT
                                    ),
                                    @ExampleObject(
                                            name = "invalidDislikedMenuItem",
                                            value = RecommendationApiExamples.GUEST_RECOMMENDATION_INVALID_DISLIKED_MENU_ITEM
                                    )
                            }
                    )
            )
    })
    ApiResponse<GuestPersonalRecommendationResponse> createGuestPersonalRecommendation(
            @Valid
            CreateGuestPersonalRecommendationRequest request
    );

    @Operation(
            summary = "내 개인 추천 이력 목록 조회",
            description = """
                    내 개인 추천 이력 목록을 조회합니다.

                    현재 로그인한 회원의 개인 추천 이력을 최신 요청 순서로 반환합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonalRecommendationSummaryPageApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = RecommendationApiExamples.PERSONAL_RECOMMENDATION_LIST_SUCCESS
                            )
                    )
            )
    })
    ApiResponse<PageResponse<PersonalRecommendationResponse>> getMyPersonalRecommendationList(
            @Parameter(description = "0부터 시작하는 페이지 번호입니다.", example = "0")
            @Min(0)
            Integer page,

            @Parameter(description = "페이지 크기입니다. 기본값은 20입니다.", example = "20")
            @Min(1)
            @Max(100)
            Integer size
    );

    @Operation(
            summary = "개인 추천 요청 생성",
            description = """
                    현재 회원의 취향 프로필과 요청 컨텍스트를 바탕으로 개인 추천을 실행합니다.
                    
                    특정 회원에게 취향 프로필(`MemberTasteProfile`) 정보가 없다면 403에러가 발생합니다.

                    `restriction ingredient`, `disliked menu item`, 최근 선택 메뉴를 제외한 뒤 후보를 저장하고 반환합니다.
                    24시간 이내 열린 개인 추천이 이미 있으면 새 추천을 만들지 않고 409를 반환합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "추천 요청 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonalRecommendationRequestApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = RecommendationApiExamples.PERSONAL_RECOMMENDATION_CREATE_SUCCESS
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "회원 취향 프로필 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "tasteProfileRequired",
                                    value = RecommendationApiExamples.PERSONAL_RECOMMENDATION_TASTE_PROFILE_REQUIRED
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "열린 개인 추천이 이미 있음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "openExists",
                                    value = RecommendationApiExamples.PERSONAL_RECOMMENDATION_OPEN_EXISTS
                            )
                    )
            )
    })
    ApiResponse<PersonalRecommendationRequestResponse> createPersonalRecommendation(
            @Valid
            CreatePersonalRecommendationRequest request
    );

    @Operation(
            summary = "개인 추천 요청 상세 조회",
            description = """
                    개인 추천 요청과 후보 목록, 선택 상태를 조회합니다.

                    본인이 생성한 개인 추천만 조회할 수 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonalRecommendationDetailApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = RecommendationApiExamples.PERSONAL_RECOMMENDATION_DETAIL_SUCCESS
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "개인 추천을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "notFound",
                                    value = RecommendationApiExamples.PERSONAL_RECOMMENDATION_NOT_FOUND
                            )
                    )
            )
    })
    ApiResponse<PersonalRecommendationDetailResponse> getPersonalRecommendation(Long requestId);

    @Operation(
            summary = "개인 추천 후보 목록 조회",
            description = """
                    개인 추천 요청의 후보 메뉴 목록만 조회합니다.

                    본인이 생성한 개인 추천의 후보만 조회할 수 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PersonalRecommendationCandidateListApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = RecommendationApiExamples.PERSONAL_RECOMMENDATION_CANDIDATES_SUCCESS
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "개인 추천을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "notFound",
                                    value = RecommendationApiExamples.PERSONAL_RECOMMENDATION_NOT_FOUND
                            )
                    )
            )
    })
    ApiResponse<PersonalRecommendationCandidateListResponse> getPersonalRecommendationCandidates(Long requestId);

    @Operation(
            summary = "개인 추천 후보 선택",
            description = """
                    개인 추천 후보 중 하나를 최종 선택으로 반영합니다.

                    선택된 후보는 개인 추천에 저장되며 `member_menu_actions`에 `CHOOSE` 로그가 함께 기록됩니다.
                    선택 성공 시 개인 추천은 `closeReason=SELECTED`, `closedAt=선택 시각`으로 종료됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "선택 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SelectPersonalRecommendationApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = RecommendationApiExamples.PERSONAL_RECOMMENDATION_SELECT_SUCCESS
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "개인 추천 또는 후보를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "notFound",
                                            value = RecommendationApiExamples.PERSONAL_RECOMMENDATION_NOT_FOUND
                                    ),
                                    @ExampleObject(
                                            name = "candidateNotFound",
                                            value = RecommendationApiExamples.PERSONAL_RECOMMENDATION_CANDIDATE_NOT_FOUND
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 종료된 개인 추천",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "alreadyClosed",
                                    value = RecommendationApiExamples.PERSONAL_RECOMMENDATION_ALREADY_CLOSED
                            )
                    )
            )
    })
    ApiResponse<SelectPersonalRecommendationResponse> selectPersonalRecommendationCandidate(
            Long requestId,
            @Valid
            SelectPersonalRecommendationRequest request
    );
}
