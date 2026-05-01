package matchuri.backend.api.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import matchuri.backend.api.auth.dto.request.FindLoginIdRequest;
import matchuri.backend.api.auth.dto.request.ResetPasswordRequest;
import matchuri.backend.api.auth.dto.response.FindLoginIdResponse;
import matchuri.backend.api.auth.dto.response.ResetPasswordResponse;
import matchuri.backend.api.auth.mapper.AuthMapper;
import matchuri.backend.domain.auth.service.AccountRecoveryService;
import matchuri.backend.domain.auth.support.token.RefreshTokenCookieService;
import matchuri.backend.global.api.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/recovery")
public class AccountRecoveryController implements AccountRecoveryApi {

    private final AccountRecoveryService accountRecoveryService;
    private final AuthMapper authMapper;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @Override
    @PostMapping("/login-id")
    public ApiResponse<FindLoginIdResponse> findLoginId(@Valid @RequestBody FindLoginIdRequest request) {
        var command = authMapper.toFindLoginIdCommand(request);
        var result = accountRecoveryService.findLoginId(command);
        return ApiResponse.success(authMapper.toFindLoginIdResponse(result));
    }

    @Override
    @PostMapping("/password")
    public ApiResponse<ResetPasswordResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletResponse httpResponse
    ) {
        var command = authMapper.toResetPasswordCommand(request);
        var result = accountRecoveryService.resetPassword(command);
        refreshTokenCookieService.clearRefreshToken(httpResponse);
        return ApiResponse.success(authMapper.toResetPasswordResponse(result));
    }
}
