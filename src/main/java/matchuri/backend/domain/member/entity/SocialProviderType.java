package matchuri.backend.domain.member.entity;

import java.util.Arrays;
import java.util.Locale;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.global.exception.AuthenticationException;

public enum SocialProviderType {
    GOOGLE,
    KAKAO,
    NAVER;

    public static SocialProviderType fromRegistrationId(String registrationId) {
        if (registrationId == null || registrationId.isBlank()) {
            throw new AuthenticationException(AuthErrorCode.OAUTH2_PROVIDER_NOT_SUPPORTED);
        }

        return Arrays.stream(values())
                .filter(provider -> provider.name().equals(registrationId.toUpperCase(Locale.ROOT)))
                .findFirst()
                .orElseThrow(() -> new AuthenticationException(AuthErrorCode.OAUTH2_PROVIDER_NOT_SUPPORTED));
    }

    public String toRegistrationId() {
        return name().toLowerCase(Locale.ROOT);
    }
}
