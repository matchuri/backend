package matchuri.backend.domain.auth.support.oauth2;

import matchuri.backend.domain.member.entity.SocialProviderType;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2UserInfoResolver implements OAuth2UserInfoResolver {

    @Override
    public boolean supports(SocialProviderType provider) {
        return provider == SocialProviderType.GOOGLE;
    }

    @Override
    public OAuth2ProviderUserInfo resolve(OAuth2User oauth2User) {
        return new OAuth2ProviderUserInfo(
                SocialProviderType.GOOGLE,
                oauth2User.getAttribute("sub"),
                oauth2User.getAttribute("email")
        );
    }
}
