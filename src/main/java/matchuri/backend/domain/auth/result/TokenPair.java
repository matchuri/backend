package matchuri.backend.domain.auth.result;

import java.time.LocalDateTime;

public record TokenPair(
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        LocalDateTime refreshTokenExpiresAt
) {
}
