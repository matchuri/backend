package matchuri.backend.domain.auth.service;

import java.time.LocalDateTime;

public record TokenPair(
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        LocalDateTime refreshTokenExpiresAt
) {
}
