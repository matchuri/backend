package matchuri.backend.api.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import matchuri.backend.api.auth.dto.docs.SendEmailVerificationApiResponse;
import matchuri.backend.api.auth.dto.request.SendEmailRequest;
import matchuri.backend.api.auth.dto.response.SendEmailResponse;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Email", description = "이메일 인증과 계정 복구 관련 API")
public interface EmailApi {

    @Operation(
            summary = "이메일 인증 코드 발송",
            description = """
                    회원가입, 로그인 ID 찾기, 비밀번호 재설정을 위한 이메일 인증 코드를 발송합니다.
                    
                    - 인증 코드 원문은 응답에 포함하지 않습니다.
                    - 계정 존재 여부와 무관하게 같은 성공 응답을 반환합니다.
                    - `RESET_PASSWORD` 목적에서는 `loginId`가 필요합니다.
                    - 같은 대상에 이미 발급된 미완료 인증 코드는 새 발송 시 만료 처리됩니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "발송 요청 접수",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SendEmailVerificationApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "accepted": true,
                                                "resendAvailableAfterSeconds": 60
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
                                                    "field": "loginId",
                                                    "reason": "RESET_PASSWORD 목적에서는 loginId가 필요합니다."
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "502",
                    description = "메일 발송 시스템 장애",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "sendFailed",
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "status": 502,
                                                "code": "AUTH_EMAIL_SEND_FAILED",
                                                "message": "이메일 발송에 실패했습니다.",
                                                "details": []
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<SendEmailResponse> sendVerificationEmail(@Valid @RequestBody SendEmailRequest request);
}
