package matchuri.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.member.entity.SocialProviderType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuth2LoginService {

    private final OAuth2MemberService oAuth2MemberService;
    private final SessionTokenService sessionTokenService;

    @Transactional
    public OAuth2LoginResult login(
            SocialProviderType provider,
            String providerUserId,
            String email,
            String nickname,
            String clientIp
    ) {
        var member = oAuth2MemberService.findOrCreateMember(provider, providerUserId, email, nickname);
        TokenPair tokenPair = sessionTokenService.issueLoginTokenPair(member);
        String exchangeCode = sessionTokenService.createExchangeCode(member, provider);

        log.info("auth event=oauth2_login_success provider={} memberId={} ip={}", provider.toRegistrationId(), member.getId(), clientIp);

        return new OAuth2LoginResult(
                member.getId(),
                tokenPair.refreshToken(),
                exchangeCode
        );
    }
}
