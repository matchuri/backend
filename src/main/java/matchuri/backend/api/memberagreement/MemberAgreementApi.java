package matchuri.backend.api.memberagreement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import matchuri.backend.api.common.docs.ErrorExamples;
import matchuri.backend.api.memberagreement.dto.docs.RequiredAgreementStatusApiResponse;
import matchuri.backend.api.memberagreement.dto.docs.SubmitRequiredAgreementsApiResponse;
import matchuri.backend.api.memberagreement.dto.request.SubmitRequiredAgreementsRequest;
import matchuri.backend.api.memberagreement.dto.response.RequiredAgreementStatusResponse;
import matchuri.backend.api.memberagreement.dto.response.SubmitRequiredAgreementsResponse;
import matchuri.backend.global.api.ApiResponse;

@Tag(name = "Member Agreement", description = "회원 필수 약관 상태 조회 및 동의 API")
public interface MemberAgreementApi {

    @Operation(
            summary = "필수 약관 상태 조회",
            description = """
                    현재 로그인한 회원의 필수 약관 완료 상태를 조회합니다.
                    
                    - 로그인은 필요하지만 필수 약관 미완료 상태에서도 호출할 수 있습니다.
                    - 프론트는 로그인 직후 이 API를 호출해 약관 동의 화면 이동 여부를 판단합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RequiredAgreementStatusApiResponse.class),
                            examples = @ExampleObject(
                                    name = "pending",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "requiredAgreementsCompleted": false,
                                                "missingAgreementTypes": [
                                                  "PRIVACY_POLICY",
                                                  "TERMS_OF_SERVICE"
                                                ]
                                              },
                                              "error": null
                                            }
                                            """
                            )
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
            )
    })
    ApiResponse<RequiredAgreementStatusResponse> getRequiredAgreementStatus();

    @Operation(
            summary = "필수 약관 동의 제출",
            description = """
                    현재 로그인한 회원의 필수 약관 동의를 저장합니다.
                    
                    - 요청에는 최신 필수 버전의 `TERMS_OF_SERVICE`, `PRIVACY_POLICY`가 모두 포함되어야 합니다.
                    - 동일 타입/버전 재제출은 멱등하게 처리합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "동의 저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubmitRequiredAgreementsApiResponse.class),
                            examples = @ExampleObject(
                                    name = "completed",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "requiredAgreementsCompleted": true,
                                                "missingAgreementTypes": [],
                                                "onboarding": {
                                                  "requiredAgreementsCompleted": true,
                                                  "nicknameCompleted": false,
                                                  "completed": false,
                                                  "nextStep": "REQUIRED_NICKNAME"
                                                },
                                                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                                "expiresIn": 3600
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 바디 형식 오류, 잘못된 약관 종류, 또는 필수 약관 누락",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
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
                                                            "field": "agreements",
                                                            "reason": "agreements는 비어 있을 수 없습니다."
                                                          }
                                                        ]
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "invalidAgreementType",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "data": null,
                                                      "error": {
                                                        "status": 400,
                                                        "code": "MEMBER_AGREEMENT_INVALID_TYPE",
                                                        "message": "유효하지 않은 약관 종류입니다. agreementType : MARKETING",
                                                        "details": []
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "requiredTypesMissing",
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
                    responseCode = "409",
                    description = "최신 필수 버전과 불일치",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "versionMismatch",
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "status": 409,
                                                "code": "MEMBER_AGREEMENT_VERSION_MISMATCH",
                                                "message": "최신 필수 약관 버전과 일치하지 않습니다. agreementType : TERMS_OF_SERVICE, requestedVersion : 2026-01-01",
                                                "details": []
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<SubmitRequiredAgreementsResponse> submitRequiredAgreements(SubmitRequiredAgreementsRequest request);
}
