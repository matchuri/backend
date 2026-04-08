package matchuri.backend.domain.auth.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.AuthErrorCode;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.global.exception.AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuth2LoginService {

    private final OAuth2MemberService oAuth2MemberService;
    private final SessionTokenService sessionTokenService;
    private final List<OAuth2UserInfoResolver> userInfoResolvers;

    @Transactional
    public OAuth2LoginResult login(
            SocialProviderType provider,
            OAuth2User oauth2User,
            String clientIp
    ) {
        OAuth2ProviderUserInfo userInfo = resolveUserInfo(provider, oauth2User);
        var member = oAuth2MemberService.findOrCreateMember(
                provider,
                userInfo.providerUserId(),
                userInfo.email(),
                userInfo.nickname()
        );
        TokenPair tokenPair = sessionTokenService.issueLoginTokenPair(member);
        String exchangeCode = sessionTokenService.createExchangeCode(member, provider);

        log.info("auth event=oauth2_login_success provider={} memberId={} ip={}", provider.toRegistrationId(), member.getId(), clientIp);

        return new OAuth2LoginResult(
                member.getId(),
                tokenPair.refreshToken(),
                exchangeCode
        );
    }

    private OAuth2ProviderUserInfo resolveUserInfo(SocialProviderType provider, OAuth2User oauth2User) {
        return userInfoResolvers.stream()
                .filter(resolver -> resolver.supports(provider))
                .findFirst()
                .orElseThrow(() -> new AuthenticationException(AuthErrorCode.OAUTH2_PROVIDER_NOT_SUPPORTED))
                .resolve(oauth2User);
    }
}
