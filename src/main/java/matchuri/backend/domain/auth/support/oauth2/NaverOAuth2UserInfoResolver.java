package matchuri.backend.domain.auth.support.oauth2;

import java.util.Map;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.global.util.TypeUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class NaverOAuth2UserInfoResolver implements OAuth2UserInfoResolver {
    @Override
    public boolean supports(SocialProviderType provider) {
        return provider == SocialProviderType.NAVER;
    }

    @Override
    public OAuth2ProviderUserInfo resolve(OAuth2User oauth2User) {
        Map<String, Object> root = oauth2User.getAttributes();
        Map<?, ?> response = TypeUtils.asMap(root.get("response"));

        return new OAuth2ProviderUserInfo(
                SocialProviderType.NAVER,
                TypeUtils.stringValue(response.get("id")),
                TypeUtils.stringValue(response.get("email"))
        );
    }
}
