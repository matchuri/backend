package matchuri.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoogleOAuth2LoginService {

    private final GoogleOAuth2Service googleOAuth2Service;
    private final SessionTokenService sessionTokenService;

    @Transactional
    public GoogleOAuth2LoginResult login(String providerUserId, String email, String nickname, String clientIp) {
        var member = googleOAuth2Service.findOrCreateMember(providerUserId, email, nickname);
        TokenPair tokenPair = sessionTokenService.issueLoginTokenPair(member);
        String exchangeCode = sessionTokenService.createExchangeCode(member, member.getSocialProviderType());

        log.info("auth event=oauth2_login_success provider=google memberId={} ip={}", member.getId(), clientIp);

        return new GoogleOAuth2LoginResult(
                member.getId(),
                tokenPair.refreshToken(),
                exchangeCode
        );
    }
}
