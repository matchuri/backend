package matchuri.backend.api.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import matchuri.backend.api.auth.dto.docs.LoginApiResponse;
import matchuri.backend.api.auth.dto.docs.LogoutApiResponse;
import matchuri.backend.api.auth.dto.request.LoginRequest;
import matchuri.backend.api.auth.dto.request.OAuth2ExchangeRequest;
import matchuri.backend.api.auth.dto.response.LoginResponse;
import matchuri.backend.api.auth.dto.response.LogoutResponse;
import matchuri.backend.global.api.ApiResponse;

@Tag(name = "Auth", description = "로그인, 로그아웃, Google OAuth2 로그인 관련 API")
public interface AuthApi {

    @Operation(
            summary = "로컬 로그인",
            description = """
                    `loginId + password`로 로그인합니다.

                    - 응답 body에는 `accessToken`과 회원 요약 정보가 포함됩니다.
                    - `refreshToken`은 응답 body가 아니라 `HttpOnly` 쿠키로 내려갑니다.
                    - 프론트는 이후 보호 API 호출 시 `Authorization: Bearer <accessToken>` 헤더를 사용합니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
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
                                                  "role": "MEMBER"
                                                }
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "아이디 또는 비밀번호가 올바르지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "loginFailed",
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "status": 401,
                                                "code": "AUTH_LOGIN_FAILED",
                                                "message": "아이디 또는 비밀번호가 올바르지 않습니다.",
                                                "details": []
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<LoginResponse> login(
            LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );

    @Operation(
            summary = "리프레시 토큰으로 Access Token 재발급",
            description = """
                    `HttpOnly` 쿠키의 `refreshToken`으로 현재 로그인 세션을 검증한 뒤 새 Access Token을 발급합니다.

                    - 요청 body는 없습니다.
                    - `refreshToken`은 요청 body가 아니라 쿠키에서 읽습니다.
                    - 성공 시 응답 body에는 새 `accessToken`과 회원 요약 정보가 포함됩니다.
                    - 성공 시 현재 로그인 세션의 `refreshToken`도 새 값으로 회전하여 쿠키를 다시 설정합니다.
                    - 유효하지 않거나 만료된 `refreshToken`이면 쿠키를 비우고 인증 실패를 반환합니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "재발급 성공",
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
                                                  "role": "MEMBER"
                                                }
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "refreshToken이 없거나, 유효하지 않거나, 만료됨"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "비활성 회원이라 재발급이 거절됨"
            )
    })
    ApiResponse<LoginResponse> refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    @Operation(
            summary = "로그아웃",
            description = """
                    현재 로그인 세션의 `refreshToken`만 폐기합니다.

                    - 서버는 `refreshToken` 쿠키를 삭제합니다.
                    - 이미 발급된 `accessToken`은 즉시 무효화되지 않으며, 만료 시점까지는 보호 API 호출에 사용할 수 있습니다.
                    - 즉, 현재 단계의 로그아웃 의미는 `재발급 차단`이지 `즉시 전역 세션 종료`가 아닙니다.
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LogoutApiResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "loggedOut": true
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "refreshToken 쿠키가 없거나 서버 저장소에서 현재 세션을 찾지 못함",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "logoutFailed",
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "status": 400,
                                                "code": "AUTH_LOGOUT_FAILED",
                                                "message": "로그아웃 처리에 실패했습니다.",
                                                "details": []
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "accessToken이 없거나 유효하지 않음"
            )
    })
    ApiResponse<LogoutResponse> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    @Operation(
            summary = "Google OAuth2 로그인 시작",
            description = """
                    Google 로그인 화면으로 리다이렉트합니다.

                    - 일반 JSON API가 아니라 `302 Redirect` 응답입니다.
                    - 브라우저 이동 또는 팝업/리다이렉트 흐름에서 사용합니다.
                    - 로그인 성공 후 프론트는 최종적으로 `code`를 전달받고, 별도 교환 API를 호출해 `accessToken`을 받습니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "302",
                    description = "Google 인증 페이지로 리다이렉트"
            )
    })
    void startGoogleOAuth2Login(HttpServletResponse response) throws IOException;

    @Operation(
            summary = "Google OAuth2 교환 코드 -> Access Token 교환",
            description = """
                    Google OAuth2 로그인 성공 후 프론트가 받은 단기 교환 코드를 Matchuri `accessToken`으로 교환합니다.

                    - 성공 시 응답 body에는 `accessToken`과 회원 요약 정보가 포함됩니다.
                    - `refreshToken`은 이미 OAuth2 성공 리다이렉트 단계에서 `HttpOnly` 쿠키로 처리되므로 body에는 포함되지 않습니다.
                    - 같은 교환 코드는 한 번만 사용할 수 있습니다.
                    - 만료되었거나 이미 사용한 코드는 `AUTH_OAUTH2_EXCHANGE_CODE_INVALID`를 반환합니다.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "교환 성공",
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
                                                  "id": 7,
                                                  "role": "MEMBER"
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
                    description = "지원하지 않는 소셜 로그인 provider"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "교환 코드가 없거나, 만료되었거나, 이미 사용되었음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "invalidExchangeCode",
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "status": 401,
                                                "code": "AUTH_OAUTH2_EXCHANGE_CODE_INVALID",
                                                "message": "유효하지 않은 소셜 로그인 교환 코드입니다.",
                                                "details": []
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<LoginResponse> exchangeOAuth2Code(
            OAuth2ExchangeRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );
}
