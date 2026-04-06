package matchuri.backend.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import matchuri.backend.api.auth.dto.LoginRequest;
import matchuri.backend.api.auth.dto.LoginResponse;
import matchuri.backend.api.auth.dto.LogoutResponse;
import matchuri.backend.api.auth.dto.OAuth2ExchangeRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    LogoutResponse logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    LoginResponse exchangeOAuth2Code(
            OAuth2ExchangeRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );
}
