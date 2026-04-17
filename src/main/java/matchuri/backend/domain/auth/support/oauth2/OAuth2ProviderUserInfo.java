package matchuri.backend.domain.auth.support.oauth2;

import matchuri.backend.domain.member.entity.SocialProviderType;

public record OAuth2ProviderUserInfo(
        SocialProviderType provider,
        String providerUserId,
        String email,
        String nickname
) {
}
