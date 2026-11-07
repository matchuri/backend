package matchuri.backend.api.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import matchuri.backend.api.auth.dto.docs.LoginApiResponse;
import matchuri.backend.api.auth.dto.request.LoginTestRequest;
import matchuri.backend.api.auth.dto.response.LoginResponse;
import matchuri.backend.api.common.docs.ErrorExamples;
import matchuri.backend.global.api.ApiResponse;

@Tag(name = "Auth Test", description = "로컬 부하 테스트 준비용 인증 API")
public interface AuthTestApi {

    @Operation(
            summary = "로컬 테스트 회원 Access Token 발급",
            description = """
                    로컬 프로필에서만 지정한 활성 회원의 JWT Access Token을 발급합니다.

                    - 비밀번호와 CAPTCHA 검증을 수행하지 않습니다.
                    - Refresh Token을 만들거나 쿠키를 설정하지 않습니다.
                    - 운영 및 공유 개발 환경에서는 endpoint 자체가 등록되지 않습니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Access Token 발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                                "refreshToken": null,
                                                "expiresIn": 3600,
                                                "member": {
                                                  "id": 1,
                                                  "role": "MEMBER",
                                                  "nickname": "테스터일"
                                                },
                                                "onboarding": {
                                                  "requiredAgreementsCompleted": true,
                                                  "nicknameCompleted": true,
                                                  "tasteProfileCompleted": true,
                                                  "completed": true,
                                                  "nextStep": "READY"
                                                }
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "memberId 요청 필드 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "invalidBodyField",
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "status": 400,
                                                "code": "COMMON_INVALID_BODY_FIELD",
                                                "message": "요청 바디 필드가 올바르지 않습니다.",
                                                "details": [
                                                  {
                                                    "source": "BODY",
                                                    "field": "memberId",
                                                    "reason": "memberId는 양수여야 합니다."
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "비활성 또는 삭제 대기 회원",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "inactiveMember", value = ErrorExamples.MEMBER_INACTIVE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "회원을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "memberNotFound",
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "status": 404,
                                                "code": "MEMBER_NOT_FOUND",
                                                "message": "해당 회원을 찾을 수 없습니다. memberId : 999999",
                                                "details": []
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<LoginResponse> loginTest(LoginTestRequest request);
}
