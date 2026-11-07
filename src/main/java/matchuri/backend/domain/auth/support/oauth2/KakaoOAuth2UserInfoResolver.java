package matchuri.backend.domain.auth.support.oauth2;

import java.util.Map;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.global.util.TypeUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class KakaoOAuth2UserInfoResolver implements OAuth2UserInfoResolver {

    @Override
    public boolean supports(SocialProviderType provider) {
        return provider == SocialProviderType.KAKAO;
    }

    @Override
    public OAuth2ProviderUserInfo resolve(OAuth2User oauth2User) {
        Map<?, ?> kakaoAccount = TypeUtils.asMap(oauth2User.getAttribute("kakao_account"));

        return new OAuth2ProviderUserInfo(
                SocialProviderType.KAKAO,
                TypeUtils.stringValue(oauth2User.getAttribute("id")),
                TypeUtils.stringValue(kakaoAccount.get("email"))
        );
    }
}
