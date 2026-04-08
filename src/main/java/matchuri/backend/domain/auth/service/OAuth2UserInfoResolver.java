package matchuri.backend.domain.auth.service;

import matchuri.backend.domain.member.entity.SocialProviderType;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserInfoResolver {

    boolean supports(SocialProviderType provider);

    OAuth2ProviderUserInfo resolve(OAuth2User oauth2User);
}
