package matchuri.backend.domain.auth.service;

import matchuri.backend.domain.auth.command.LoginCommand;
import matchuri.backend.domain.auth.command.OAuth2ExchangeCommand;
import matchuri.backend.domain.auth.result.LoginPayload;
import matchuri.backend.domain.auth.result.LoginResult;
import matchuri.backend.domain.auth.result.LogoutResult;

public interface AuthService {

    LoginResult login(LoginCommand command, String clientIp);

    LoginResult refresh(String refreshToken, String clientIp);

    LogoutResult logout(String refreshToken, String clientIp);

    LoginPayload exchangeOAuth2Code(OAuth2ExchangeCommand command, String clientIp);
}
