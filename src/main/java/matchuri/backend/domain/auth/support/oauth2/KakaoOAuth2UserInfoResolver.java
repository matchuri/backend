package matchuri.backend.domain.auth.support.oauth2;

import java.util.Map;
import matchuri.backend.domain.member.entity.SocialProviderType;
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
        Map<?, ?> kakaoAccount = asMap(oauth2User.getAttribute("kakao_account"));
        Map<?, ?> properties = asMap(oauth2User.getAttribute("properties"));
        Map<?, ?> profile = asMap(kakaoAccount.get("profile"));

        return new OAuth2ProviderUserInfo(
                SocialProviderType.KAKAO,
                stringValue(oauth2User.getAttribute("id")),
                stringValue(kakaoAccount.get("email")),
                firstPresent(
                        stringValue(properties.get("nickname")),
                        stringValue(profile.get("nickname")),
                        stringValue(kakaoAccount.get("name"))
                )
        );
    }

    private Map<?, ?> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return map;
        }

        return Map.of();
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }

        String stringValue = String.valueOf(value);
        if (stringValue.isBlank()) {
            return null;
        }

        return stringValue;
    }

    private String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }
}
