package matchuri.backend.api.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import matchuri.backend.api.common.docs.ErrorExamples;
import matchuri.backend.api.member.dto.docs.CreateMemberApiResponse;
import matchuri.backend.api.member.dto.docs.LoginIdExistsApiResponse;
import matchuri.backend.api.member.dto.docs.MemberTasteProfileSummaryApiResponse;
import matchuri.backend.api.member.dto.docs.NicknameExistsApiResponse;
import matchuri.backend.api.member.dto.docs.RegisterLocalMemberApiResponse;
import matchuri.backend.api.member.dto.request.CreateMemberRequest;
import matchuri.backend.api.member.dto.request.RegisterLocalMemberRequest;
import matchuri.backend.api.member.dto.request.UpdateMemberBasicInfoRequest;
import matchuri.backend.api.member.dto.request.UpdateMemberTasteProfileRequest;
import matchuri.backend.api.member.dto.response.CreateMemberResponse;
import matchuri.backend.api.member.dto.response.LoginIdExistsResponse;
import matchuri.backend.api.member.dto.response.MemberProfileResponse;
import matchuri.backend.api.member.dto.response.MemberTasteProfileSummaryResponse;
import matchuri.backend.api.member.dto.response.NicknameExistsResponse;
import matchuri.backend.api.member.dto.response.RegisterLocalMemberResponse;
import matchuri.backend.api.member.dto.response.UpdateMemberResponse;
import matchuri.backend.api.member.dto.response.WithdrawMemberResponse;
import matchuri.backend.global.api.ApiResponse;

@Tag(name = "Member", description = "회원 관련 공개/인증 API")
public interface MemberApi {

    @Operation(
            summary = "자체 회원가입 통합",
            description = """
                    자체 회원가입에서 `loginId`, `password`, `nickname`, 검증된 `email`, 필수 약관 동의를 하나의 요청으로 원자적으로 처리합니다.
                    
                    - 가입 성공 시 자동 로그인되지 않습니다.
                    - 필수 약관 2종과 최신 버전이 모두 포함되어야 합니다.
                    - 닉네임은 기본값 없이 필수 입력입니다.
                    - 회원 생성 전에 `SIGNUP` 목적의 `emailVerificationToken`이 필요합니다.
                    - 한 이메일에 여러 자체 로그인 ID는 허용하지 않습니다.
                    - 처리 중 하나라도 실패하면 회원과 약관 동의 기록은 저장되지 않습니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "통합 회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterLocalMemberApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "memberId": 1,
                                                "loginId": "tester01",
                                                "email": "tester@example.com",
                                                "nickname": "점심탐험가",
                                                "createdAt": "2026-04-14T20:15:30"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "필수 약관 동의 요청 누락 또는 형식 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "requiredAgreementTypesMissing",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "data": null,
                                                      "error": {
                                                        "status": 400,
                                                        "code": "MEMBER_AGREEMENT_REQUIRED_TYPES_MISSING",
                                                        "message": "필수 약관 동의 요청이 누락되었습니다. missingTypes : [PRIVACY_POLICY]",
                                                        "details": []
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "이메일 인증 token이 없거나, 만료되었거나, 요청 이메일과 맞지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "emailVerificationFailed",
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "status": 401,
                                                "code": "AUTH_EMAIL_VERIFICATION_FAILED",
                                                "message": "이메일 인증에 실패했습니다.",
                                                "details": []
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 사용 중인 loginId, nickname 또는 email",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "duplicateLoginId",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "data": null,
                                                      "error": {
                                                        "status": 409,
                                                        "code": "MEMBER_DUPLICATE_LOGIN_ID",
                                                        "message": "이미 사용 중인 로그인 아이디입니다. loginId : tester01",
                                                        "details": []
                                                      }
                                                    }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "duplicateNickname",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "data": null,
                                                      "error": {
                                                        "status": 409,
                                                        "code": "MEMBER_DUPLICATE_NICKNAME",
                                                        "message": "이미 사용 중인 닉네임입니다. nickname : 점심탐험가",
                                                        "details": []
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "duplicateEmail",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "data": null,
                                                      "error": {
                                                        "status": 409,
                                                        "code": "MEMBER_DUPLICATE_EMAIL",
                                                        "message": "이미 사용 중인 이메일입니다. email : tester@example.com",
                                                        "details": []
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ApiResponse<RegisterLocalMemberResponse> registerLocalMember(RegisterLocalMemberRequest request);

    @Operation(
            summary = "회원 가입 레거시 생성",
            description = """
                    일반 회원 계정을 최소 정보(`loginId`, `password`)만으로 생성합니다.
                    
                    - 가입 성공 시 자동 로그인되지 않습니다.
                    - 필수 약관 동의와 닉네임 입력은 포함되지 않습니다.
                    - 신규 구현은 `POST /api/v1/members/signup` 사용을 우선 권장합니다.
                    - 응답에는 생성된 회원의 `memberId`, `loginId`, `createdAt`이 포함됩니다.
                    - 가입 직후 로그인 화면으로 이동하거나, 같은 `loginId`로 로그인 API를 이어 호출하면 됩니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원 가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateMemberApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "memberId": 1,
                                                "loginId": "tester01",
                                                "createdAt": "2026-04-07T10:15:30"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 사용 중인 loginId",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "duplicateLoginId",
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "status": 409,
                                                "code": "MEMBER_DUPLICATE_LOGIN_ID",
                                                "message": "이미 사용 중인 로그인 아이디입니다. loginId : tester01",
                                                "details": []
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<CreateMemberResponse> createMember(CreateMemberRequest request);

    @Operation(
            summary = "로그인 ID 중복 확인",
            description = """
                    회원 가입 시 사용할 로그인 ID가 이미 사용 중인지 확인합니다.
                    이 API는 인증 없이 호출할 수 있습니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "정상 조회",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginIdExistsApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "loginId": "tester01",
                                                "exists": true
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "로그인 ID 형식이 올바르지 않은 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "invalidLoginId",
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "status": 400,
                                                "code": "COMMON_INVALID_PATH_VARIABLE",
                                                "message": "경로 변수 형식이 올바르지 않습니다",
                                                "details": [
                                                  {
                                                    "source": "PATH",
                                                    "field": "loginId",
                                                    "reason": "로그인 아이디는 영문, 숫자, 점(.), 밑줄(_), 하이픈(-)만 사용할 수 있습니다."
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<LoginIdExistsResponse> checkLoginIdExists(
            @Parameter(
                    description = """
                            회원 가입 시 사용할 로그인 ID입니다.
                            
                            제약:
                            - 1자 이상 50자 이하
                            - 공백 불가
                            - 허용 문자: 영문 대소문자, 숫자, 점(.), 밑줄(_), 하이픈(-)
                            - 서버 정규식 검증: ^[A-Za-z0-9._-]+$
                            - 허용 예시: tester01, matchuri-user, user.name
                            - 비허용 예시: test user, 한글아이디, 50자 초과 문자열
                            """,
                    example = "tester01"
            )
            String loginId
    );

    @Operation(
            summary = "닉네임 중복 확인",
            description = """
                    프로필 설정 시 사용할 닉네임이 이미 사용 중인지 확인합니다.
                    이 API는 인증 없이 호출할 수 있습니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "정상 조회",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NicknameExistsApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "nickname": "example_google",
                                                "exists": true
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "닉네임 형식이 올바르지 않은 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "invalidNickname",
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "status": 400,
                                                "code": "COMMON_INVALID_PATH_VARIABLE",
                                                "message": "경로 변수 형식이 올바르지 않습니다",
                                                "details": [
                                                  {
                                                    "source": "PATH",
                                                    "field": "nickname",
                                                    "reason": "닉네임은 비어 있을 수 없습니다."
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<NicknameExistsResponse> checkNicknameExists(
            @Parameter(
                    description = """
                            프로필 설정 시 사용할 닉네임입니다.
                            
                            제약:
                            - 공백만으로 구성될 수 없음
                            - 최대 100자
                            - 예시: 점심탐험가, example_google
                            """,
                    example = "example_google"
            )
            String nickname
    );

    @Operation(
            summary = "내 프로필 조회",
            description = """
                    현재 로그인한 회원의 기본 프로필 정보를 조회합니다.
                    
                    - `Authorization: Bearer <accessToken>` 헤더가 필요합니다.
                    - 현재 단계에서는 최소 프로필과 로그인 유형 판단에 필요한 `id`, `nickname`, `isSocial`을 반환합니다.
                    - `loginId`, `email`, 취향 프로필 상세 필드는 이 응답에 포함되지 않습니다.
                    """)
    ApiResponse<MemberProfileResponse> getMyProfile();

    @Operation(
            summary = "내 취향 프로필 조회",
            description = """
                    현재 로그인한 회원의 취향 프로필을 조회합니다.
                    
                    - `Authorization: Bearer <accessToken>` 헤더가 필요합니다.
                    - 프로필이 아직 없어도 빈 배열 기반의 정상 응답을 반환합니다.
                    - 선택된 `attribute category`, `restriction ingredient`, `disliked menu item`은 표시용 최소 메타데이터와 함께 반환합니다.
                    - `profileVersion`은 현재 프로필 정책/구조가 어떤 버전을 따르는지 나타내는 서버 관리 버전입니다.
                    - 단순 사용자 입력 변경만으로는 `profileVersion`이 바뀌지 않습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MemberTasteProfileSummaryApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "profileExists",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": {
                                                        "memberId": 1,
                                                        "profileVersion": "v1",
                                                        "attributeCategories": [
                                                          {
                                                            "id": 1,
                                                            "categoryType": "FLAVOR",
                                                            "code": "SPICY",
                                                            "name": "매운맛",
                                                            "sortOrder": 10
                                                          }
                                                        ],
                                                        "restrictionIngredients": [
                                                          {
                                                            "id": 101,
                                                            "code": "PEANUT",
                                                            "name": "땅콩",
                                                            "allergen": true,
                                                            "sortOrder": 10
                                                          }
                                                        ],
                                                        "dislikedMenuItems": [
                                                          {
                                                            "id": 1001,
                                                            "code": "PORK_CUTLET",
                                                            "name": "돈까스"
                                                          }
                                                        ],
                                                        "updatedAt": "2026-04-17T12:30:45"
                                                      },
                                                      "error": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "emptyProfile",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": {
                                                        "memberId": 1,
                                                        "profileVersion": "v1",
                                                        "attributeCategories": [],
                                                        "restrictionIngredients": [],
                                                        "dislikedMenuItems": [],
                                                        "updatedAt": null
                                                      },
                                                      "error": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "accessToken이 없거나 유효하지 않거나 만료됨",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "tokenMissing", value = ErrorExamples.AUTH_TOKEN_MISSING),
                                    @ExampleObject(name = "tokenInvalid", value = ErrorExamples.AUTH_TOKEN_INVALID),
                                    @ExampleObject(name = "tokenExpired", value = ErrorExamples.AUTH_TOKEN_EXPIRED)
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "필수 온보딩 미완료 또는 비활성 회원",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "requiredAgreement", value = ErrorExamples.MEMBER_AGREEMENT_REQUIRED),
                                    @ExampleObject(name = "nicknameRequired", value = ErrorExamples.MEMBER_NICKNAME_REQUIRED),
                                    @ExampleObject(name = "inactiveMember", value = ErrorExamples.MEMBER_INACTIVE)
                            }
                    )
            )
    })
    ApiResponse<MemberTasteProfileSummaryResponse> getMyTasteProfile();

    @Operation(
            summary = "내 기본 정보 수정",
            description = """
                    현재 로그인한 회원의 기본 정보 중 `nickname`만 수정합니다.
                    
                    - 부분 수정 API이므로 필요한 필드만 보내면 됩니다.
                    - `nickname`을 보내지 않으면 변경하지 않습니다.
                    - 약관 또는 닉네임 온보딩 미완료 상태에서도 인증된 회원이면 닉네임 확정을 위해 호출할 수 있습니다.
                    - 닉네임 수정 성공 시 닉네임 온보딩 완료 상태로 처리됩니다.
                    - 성공 시 최신 수정 시각(`updatedAt`)을 반환합니다.
                    """)
    ApiResponse<UpdateMemberResponse> updateMyProfile(UpdateMemberBasicInfoRequest request);

    @Operation(
            summary = "내 취향 프로필 전체 교체 저장",
            description = """
                    현재 로그인한 회원의 취향 프로필을 전체 교체 방식으로 저장합니다.
                    
                    - `attributeCategoryIds`, `restrictionIngredientIds`, `dislikedMenuItemIds`는 각각 최신 입력 기준으로 전체 교체됩니다.
                    - 특정 목록을 비우려면 빈 배열을 보내야 합니다.
                    - 존재하지 않거나 비활성화된 참조 데이터 ID는 거절됩니다.
                    - `dislikedMenuItemIds`는 활성 `MenuItem` 검색/선택 결과의 ID 목록입니다.
                    - 성공 시 조회 API와 동일한 구조를 반환합니다.
                    - `profileVersion`은 수정 시각 대체값이 아니라 프로필 정책/구조 버전이므로, 단순 저장만으로는 바뀌지 않습니다.
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MemberTasteProfileSummaryApiResponse.class),
                            examples = @ExampleObject(
                                    name = "saved",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "memberId": 1,
                                                "profileVersion": "v1",
                                                "attributeCategories": [
                                                  {
                                                    "id": 1,
                                                    "categoryType": "FLAVOR",
                                                    "code": "SPICY",
                                                    "name": "매운맛",
                                                    "sortOrder": 10
                                                  }
                                                ],
                                                "restrictionIngredients": [
                                                  {
                                                    "id": 101,
                                                    "code": "PEANUT",
                                                    "name": "땅콩",
                                                    "allergen": true,
                                                    "sortOrder": 10
                                                  }
                                                ],
                                                "dislikedMenuItems": [
                                                  {
                                                    "id": 1001,
                                                    "code": "PORK_CUTLET",
                                                    "name": "돈까스"
                                                  }
                                                ],
                                                "updatedAt": "2026-04-20T18:00:00"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "중복 ID 또는 잘못된 참조 데이터 입력",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "duplicateAttributeCategory",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "data": null,
                                                      "error": {
                                                        "status": 400,
                                                        "code": "MEMBER_DUPLICATE_TASTE_ATTRIBUTE_CATEGORY",
                                                        "message": "중복된 attribute category ID가 포함되어 있습니다. attributeCategoryIds : [1, 1]",
                                                        "details": []
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "invalidAttributeCategory",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "data": null,
                                                      "error": {
                                                        "status": 400,
                                                        "code": "MEMBER_INVALID_TASTE_ATTRIBUTE_CATEGORY",
                                                        "message": "유효하지 않거나 비활성화된 attribute category ID가 포함되어 있습니다. attributeCategoryIds : [999]",
                                                        "details": []
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "invalidRestrictionIngredient",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "data": null,
                                                      "error": {
                                                        "status": 400,
                                                        "code": "MEMBER_INVALID_TASTE_RESTRICTION_INGREDIENT",
                                                        "message": "유효하지 않거나 비활성화된 restriction ingredient ID가 포함되어 있습니다. restrictionIngredientIds : [999]",
                                                        "details": []
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "duplicateRestrictionIngredient",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "data": null,
                                                      "error": {
                                                        "status": 400,
                                                        "code": "MEMBER_DUPLICATE_TASTE_RESTRICTION_INGREDIENT",
                                                        "message": "중복된 restriction ingredient ID가 포함되어 있습니다. restrictionIngredientIds : [101, 101]",
                                                        "details": []
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "duplicateDislikedMenuItem",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "data": null,
                                                      "error": {
                                                        "status": 400,
                                                        "code": "MEMBER_DUPLICATE_TASTE_DISLIKED_MENU_ITEM",
                                                        "message": "중복된 disliked menu item ID가 포함되어 있습니다. dislikedMenuItemIds : [1001, 1001]",
                                                        "details": []
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "invalidDislikedMenuItem",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "data": null,
                                                      "error": {
                                                        "status": 400,
                                                        "code": "MEMBER_INVALID_TASTE_DISLIKED_MENU_ITEM",
                                                        "message": "유효하지 않거나 비활성화된 disliked menu item ID가 포함되어 있습니다. dislikedMenuItemIds : [999]",
                                                        "details": []
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "invalidBodyField",
                                            value = ErrorExamples.COMMON_INVALID_BODY_FIELD
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "accessToken이 없거나 유효하지 않거나 만료됨",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "tokenMissing", value = ErrorExamples.AUTH_TOKEN_MISSING),
                                    @ExampleObject(name = "tokenInvalid", value = ErrorExamples.AUTH_TOKEN_INVALID),
                                    @ExampleObject(name = "tokenExpired", value = ErrorExamples.AUTH_TOKEN_EXPIRED)
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "필수 온보딩 미완료 또는 비활성 회원",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "requiredAgreement", value = ErrorExamples.MEMBER_AGREEMENT_REQUIRED),
                                    @ExampleObject(name = "nicknameRequired", value = ErrorExamples.MEMBER_NICKNAME_REQUIRED),
                                    @ExampleObject(name = "inactiveMember", value = ErrorExamples.MEMBER_INACTIVE)
                            }
                    )
            )
    })
    ApiResponse<MemberTasteProfileSummaryResponse> updateMyTasteProfile(UpdateMemberTasteProfileRequest request);

    @Operation(
            summary = "회원 탈퇴",
            description = """
                    현재 로그인한 회원을 비활성화 처리합니다.
                    
                    - 물리 삭제가 아니라 `status=INACTIVE`로 전환됩니다.
                    - 탈퇴 후 같은 계정으로 다시 로그인할 수 없습니다.
                    - 이미 발급된 access token이 남아 있어도 이후 보호 API에서는 비활성 회원으로 거절됩니다.
                    """)
    ApiResponse<WithdrawMemberResponse> withdraw();
}
