package matchuri.backend.global.security;

import matchuri.backend.domain.member.entity.SocialProviderType;

public interface UnlinkClient {
    boolean support(boolean isSocial, SocialProviderType socialProviderType);

    void unlink(String providerId);
}
