package matchuri.backend.api.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import matchuri.backend.api.member.dto.CreateMemberRequest;
import matchuri.backend.api.member.dto.CreateMemberResponse;
import matchuri.backend.api.member.dto.LoginIdExistsResponse;
import matchuri.backend.api.member.dto.MemberProfileResponse;
import matchuri.backend.api.member.dto.NicknameExistsResponse;
import matchuri.backend.api.member.dto.UpdateMemberBasicInfoRequest;
import matchuri.backend.api.member.dto.UpdateMemberResponse;
import matchuri.backend.api.member.dto.UpdateMemberTasteProfileRequest;
import matchuri.backend.api.member.dto.WithdrawMemberResponse;
import matchuri.backend.global.api.ApiResponse;

@Tag(name = "Member", description = "회원 관련 공개/인증 API")
public interface MemberApi {

    @Operation(
            summary = "회원 가입",
            description = """
                    일반 회원 계정을 생성합니다.

                    - 가입 성공 시 자동 로그인되지 않습니다.
                    - 응답에는 생성된 회원의 `memberId`, `loginId`, `createdAt`이 포함됩니다.
                    - 가입 직후 로그인 화면으로 이동하거나, 같은 `loginId`로 로그인 API를 이어 호출하면 됩니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원 가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateMemberResponse.class),
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
                    """,
            security = {}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "정상 조회",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginIdExistsResponse.class),
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
                    """,
            security = {}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "정상 조회",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NicknameExistsResponse.class),
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
                    - 현재 단계에서는 최소 프로필만 반환하므로 `id`, `nickname`만 사용하면 됩니다.
                    - `loginId`, `email`, 취향 프로필 상세 필드는 이 응답에 포함되지 않습니다.
                    """)
    ApiResponse<MemberProfileResponse> getMyProfile();

    @Operation(
            summary = "내 기본 정보 수정",
            description = """
                    현재 로그인한 회원의 기본 정보 중 `nickname`만 수정합니다.

                    - 부분 수정 API이므로 필요한 필드만 보내면 됩니다.
                    - `nickname`을 보내지 않으면 변경하지 않습니다.
                    - 성공 시 최신 수정 시각(`updatedAt`)을 반환합니다.
                    """)
    ApiResponse<UpdateMemberResponse> updateMyProfile(UpdateMemberBasicInfoRequest request);

    @Operation(
            summary = "내 취향 프로필 수정",
            description = """
                    현재 로그인한 회원의 취향 프로필 최소 정보(`profileVersion`)를 수정합니다.

                    - 현재 단계에서는 취향 프로필 전체가 아니라 서버가 이해하는 버전 문자열만 받습니다.
                    - 취향 설문 결과를 저장한 뒤 이 API로 버전을 연결하는 식으로 사용할 수 있습니다.
                    """)
    ApiResponse<UpdateMemberResponse> updateMyTasteProfile(UpdateMemberTasteProfileRequest request);

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
