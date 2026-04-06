package matchuri.backend.api.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import matchuri.backend.api.auth.dto.LoginRequest;
import matchuri.backend.api.auth.dto.LoginResponse;
import matchuri.backend.api.auth.dto.LogoutResponse;
import matchuri.backend.api.auth.dto.OAuth2ExchangeRequest;
import matchuri.backend.global.api.ApiResponse;

public interface AuthApi {

    ApiResponse<LoginResponse> login(
            LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );

    ApiResponse<LogoutResponse> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    void startGoogleOAuth2Login(HttpServletResponse response) throws IOException;

    ApiResponse<LoginResponse> exchangeOAuth2Code(
            OAuth2ExchangeRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );
}
