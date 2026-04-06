package matchuri.backend.api.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.auth.dto.LoginRequest;
import matchuri.backend.api.auth.dto.LoginResponse;
import matchuri.backend.api.auth.dto.LogoutResponse;
import matchuri.backend.api.auth.dto.OAuth2ExchangeRequest;
import matchuri.backend.api.member.mapper.MemberMapper;
import matchuri.backend.domain.auth.service.AuthService;
import matchuri.backend.domain.auth.service.LoginResult;
import matchuri.backend.domain.auth.service.RefreshTokenCookieService;
import matchuri.backend.global.api.ApiResponse;
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
        LoginResult loginResult = authService.login(command, resolveClientIp(httpRequest));
        LoginResponse response = memberMapper.toLoginResponse(loginResult.payload());

        refreshTokenCookieService.addRefreshToken(httpResponse, loginResult.refreshToken());
        return ApiResponse.success(response);
    }

    @Override
    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String refreshToken = refreshTokenCookieService.resolveRefreshToken(httpRequest)
                .orElseThrow(() -> new matchuri.backend.global.exception.AuthenticationException(matchuri.backend.domain.auth.AuthErrorCode.LOGOUT_FAILED));

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
