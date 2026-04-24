package matchuri.backend.api.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.auth.dto.request.LoginRequest;
import matchuri.backend.api.auth.dto.request.OAuth2ExchangeRequest;
import matchuri.backend.api.auth.dto.response.LoginResponse;
import matchuri.backend.api.auth.dto.response.LogoutResponse;
import matchuri.backend.api.member.mapper.MemberMapper;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.auth.service.AuthService;
import matchuri.backend.domain.auth.support.token.RefreshTokenCookieService;
import matchuri.backend.global.api.ApiResponse;
import matchuri.backend.global.exception.AuthenticationException;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final RefreshTokenCookieService refreshTokenCookieService;
    private final MemberMapper memberMapper;

    @Override
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        var command = memberMapper.toLoginCommand(request.loginId(), request.password());
        var result = authService.login(command, resolveClientIp(httpRequest));
        LoginResponse response = memberMapper.toLoginResponse(result.payload());

        refreshTokenCookieService.addRefreshToken(httpResponse, result.refreshToken());
        return ApiResponse.success(response);
    }

    @Override
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String refreshToken = refreshTokenCookieService.resolveRefreshToken(httpRequest)
                .orElseThrow(() -> new AuthenticationException(AuthErrorCode.REFRESH_TOKEN_MISSING));

        try {
            var result = authService.refresh(refreshToken, resolveClientIp(httpRequest));
            LoginResponse response = memberMapper.toLoginResponse(result.payload());

            refreshTokenCookieService.addRefreshToken(httpResponse, result.refreshToken());
            return ApiResponse.success(response);
        } catch (AuthenticationException | BusinessException exception) {
            refreshTokenCookieService.clearRefreshToken(httpResponse);
            throw exception;
        }
    }

    @Override
    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String refreshToken = refreshTokenCookieService.resolveRefreshToken(httpRequest)
                .orElseThrow(() -> new matchuri.backend.global.exception.AuthenticationException(
                        AuthErrorCode.LOGOUT_FAILED));

        var result = authService.logout(refreshToken, resolveClientIp(httpRequest));
        LogoutResponse response = memberMapper.toLogoutResponse(result);

        refreshTokenCookieService.clearRefreshToken(httpResponse);
        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/oauth2/google")
    public void startGoogleOAuth2Login(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    @Override
    @PostMapping("/oauth2/exchange")
    public ApiResponse<LoginResponse> exchangeOAuth2Code(
            @Valid @RequestBody OAuth2ExchangeRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        var command = memberMapper.toOAuth2ExchangeCommand(request.provider(), request.code());
        var payload = authService.exchangeOAuth2Code(command, resolveClientIp(httpRequest));
        LoginResponse response = memberMapper.toLoginResponse(payload);

        return ApiResponse.success(response);
    }

    private String resolveClientIp(HttpServletRequest httpRequest) {
        return httpRequest.getRemoteAddr();
    }
}
