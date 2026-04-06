package matchuri.backend.domain.auth.service;

import matchuri.backend.api.auth.dto.LoginResponse;

public record LoginResult(
        LoginResponse response,
        String refreshToken
) {
}
