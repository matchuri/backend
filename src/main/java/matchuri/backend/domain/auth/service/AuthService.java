package matchuri.backend.domain.auth.service;

import matchuri.backend.api.auth.dto.LoginRequest;
import matchuri.backend.api.auth.dto.LoginResponse;
import matchuri.backend.api.auth.dto.LogoutResponse;
import matchuri.backend.api.auth.dto.OAuth2ExchangeRequest;

public interface AuthService {

    LoginResult login(LoginRequest request, String clientIp);

    LogoutResponse logout(String refreshToken, String clientIp);

    LoginResponse exchangeOAuth2Code(OAuth2ExchangeRequest request, String clientIp);
}
