package matchuri.backend.global.security;

import matchuri.backend.domain.member.entity.SocialProviderType;

public interface UnlinkClient {
    boolean support(SocialProviderType socialProviderType);

    void unlink(Long memberId);
}
