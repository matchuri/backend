package matchuri.backend.api.auth;

import matchuri.backend.api.auth.dto.LoginRequest;
import matchuri.backend.api.auth.dto.LoginResponse;
import matchuri.backend.api.auth.dto.LogoutResponse;
import matchuri.backend.global.api.ApiResponse;

public interface AuthApi {

    ApiResponse<LoginResponse> login(LoginRequest request);

    ApiResponse<LogoutResponse> logout();
}
