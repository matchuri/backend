package matchuri.backend.domain.auth.service;

public interface AuthService {

    LoginResult login(LoginCommand command, String clientIp);

    LogoutResult logout(String refreshToken, String clientIp);

    LoginPayload exchangeOAuth2Code(OAuth2ExchangeCommand command, String clientIp);
}
