package matchuri.backend.domain.auth.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.AuthErrorCode;
import matchuri.backend.domain.auth.entity.AuthExchangeCode;
import matchuri.backend.domain.auth.entity.AuthRefreshToken;
import matchuri.backend.domain.auth.repository.AuthExchangeCodeRepository;
import matchuri.backend.domain.auth.repository.AuthRefreshTokenRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.global.config.MatchuriProperties;
import matchuri.backend.global.exception.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionTokenService {

    private final AuthRefreshTokenRepository authRefreshTokenRepository;
    private final AuthExchangeCodeRepository authExchangeCodeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final MatchuriProperties matchuriProperties;
    private final Clock clock = Clock.systemUTC();

    @Transactional
    public TokenPair issueLoginTokenPair(Member member) {
        TokenPair tokenPair = jwtTokenProvider.issueTokenPair(member);
        authRefreshTokenRepository.save(AuthRefreshToken.issue(member, tokenPair.refreshToken(), tokenPair.refreshTokenExpiresAt()));
        return tokenPair;
    }

    @Transactional
    public String createExchangeCode(Member member, SocialProviderType provider) {
        String code = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now(clock)
                .plusSeconds(matchuriProperties.getAuth().getOauth2().getExchangeCodeExpirationSeconds());
        authExchangeCodeRepository.save(AuthExchangeCode.issue(member, provider, code, expiresAt));
        return code;
    }

    @Transactional
    public Member consumeExchangeCode(SocialProviderType provider, String code) {
        AuthExchangeCode exchangeCode = authExchangeCodeRepository.findByCode(code)
                .orElseThrow(() -> new AuthenticationException(AuthErrorCode.OAUTH2_EXCHANGE_CODE_INVALID));

        LocalDateTime now = LocalDateTime.now(clock);
        if (exchangeCode.isUsed() || exchangeCode.isExpired(now) || exchangeCode.getProvider() != provider) {
            throw new AuthenticationException(AuthErrorCode.OAUTH2_EXCHANGE_CODE_INVALID);
        }

        exchangeCode.markUsed(now);
        return exchangeCode.getMember();
    }

    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthenticationException(AuthErrorCode.LOGOUT_FAILED);
        }

        authRefreshTokenRepository.findByToken(refreshToken)
                .ifPresentOrElse(
                        token -> authRefreshTokenRepository.delete(token),
                        () -> {
                            throw new AuthenticationException(AuthErrorCode.LOGOUT_FAILED);
                        }
                );
    }
}
