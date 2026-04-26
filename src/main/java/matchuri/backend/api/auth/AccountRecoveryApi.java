package matchuri.backend.api.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import matchuri.backend.api.auth.dto.docs.FindLoginIdApiResponse;
import matchuri.backend.api.auth.dto.docs.ResetPasswordApiResponse;
import matchuri.backend.api.auth.dto.request.FindLoginIdRequest;
import matchuri.backend.api.auth.dto.request.ResetPasswordRequest;
import matchuri.backend.api.auth.dto.response.FindLoginIdResponse;
import matchuri.backend.api.auth.dto.response.ResetPasswordResponse;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Account Recovery", description = "로그인 ID 찾기와 비밀번호 재설정 관련 API")
public interface AccountRecoveryApi {

    @Operation(
            summary = "로그인 ID 찾기",
            description = """
                    이메일 인증 확인 후 발급받은 `emailVerificationToken`으로 자체 로그인 ID를 조회합니다.
                    
                    - token은 `FIND_LOGIN_ID` 목적이어야 합니다.
                    - token은 만료되지 않았고 아직 사용되지 않은 상태여야 합니다.
                    - 성공 시 token은 사용 완료 처리되어 재사용할 수 없습니다.
                    - 한 이메일에 여러 자체 로그인 ID는 허용하지 않으므로 단일 `loginId`를 반환합니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 ID 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FindLoginIdApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "loginId": "tester01"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 바디 형식 오류",
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
                                                    "field": "emailVerificationToken",
                                                    "reason": "emailVerificationToken은 비어 있을 수 없습니다."
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "이메일 인증 token이 없거나, 목적이 다르거나, 만료/사용된 상태",
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
            )
    })
    ApiResponse<FindLoginIdResponse> findLoginId(@Valid @RequestBody FindLoginIdRequest request);

    @Operation(
            summary = "비밀번호 재설정",
            description = """
                    이메일 인증 확인 후 발급받은 `emailVerificationToken`으로 자체 로그인 계정의 비밀번호를 재설정합니다.
                    
                    - token은 `RESET_PASSWORD` 목적이어야 합니다.
                    - token의 `loginId`, `email`이 요청 계정과 일치해야 합니다.
                    - 성공 시 token은 사용 완료 처리되어 재사용할 수 없습니다.
                    - 성공 시 해당 회원의 기존 refresh token 전체를 폐기합니다.
                    - 비밀번호 재설정 후 자동 로그인하거나 access token을 발급하지 않습니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 재설정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResetPasswordApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "reset": true
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 바디 형식 오류",
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
                                                    "field": "newPassword",
                                                    "reason": "newPassword는 8자 이상 100자 이하여야 합니다."
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "이메일 인증 token이 없거나, 목적/계정이 다르거나, 만료/사용된 상태",
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
            )
    })
    ApiResponse<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request);
}
