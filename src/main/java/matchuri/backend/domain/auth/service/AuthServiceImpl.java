package matchuri.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.command.LoginCommand;
import matchuri.backend.domain.auth.command.OAuth2ExchangeCommand;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.auth.result.IssuedAccessToken;
import matchuri.backend.domain.auth.result.LoginPayload;
import matchuri.backend.domain.auth.result.LoginResult;
import matchuri.backend.domain.auth.result.LogoutResult;
import matchuri.backend.domain.auth.result.TokenPair;
import matchuri.backend.domain.auth.support.token.JwtTokenProvider;
import matchuri.backend.domain.auth.support.token.SessionTokenService;
import matchuri.backend.domain.member.exception.MemberErrorCode;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.exception.AuthenticationException;
import matchuri.backend.global.exception.BusinessException;
import matchuri.backend.global.security.AuthenticatedMember;
import matchuri.backend.global.security.AuthenticationFacade;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionTokenService sessionTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @Transactional
    public LoginResult login(LoginCommand command, String clientIp) {
        Member member = memberRepository.findByLoginId(command.loginId())
                .orElseThrow(() -> new AuthenticationException(AuthErrorCode.LOGIN_FAILED));

        ensureActive(member);

        if (!passwordEncoder.matches(command.password(), member.getPasswordHash())) {
            throw new AuthenticationException(AuthErrorCode.LOGIN_FAILED);
        }

        TokenPair tokenPair = sessionTokenService.issueLoginTokenPair(member);
        log.info("auth event=login_success provider=local memberId={} ip={}", member.getId(), clientIp);

        return LoginResult.from(tokenPair, member);
    }

    @Override
    @Transactional
    public LoginResult refresh(String refreshToken, String clientIp) {
        Member member = sessionTokenService.validateRefreshToken(refreshToken);
        ensureActive(member);
        TokenPair tokenPair = sessionTokenService.rotateRefreshToken(refreshToken, member);

        log.info("auth event=refresh_success memberId={} ip={}", member.getId(), clientIp);

        return new LoginResult(
                new LoginPayload(
                        tokenPair.accessToken(),
                        tokenPair.accessTokenExpiresIn(),
                        member.getId(),
                        member.getMemberRole().name()
                ),
                tokenPair.refreshToken()
        );
    }

    @Override
    @Transactional
    public LogoutResult logout(String refreshToken, String clientIp) {
        AuthenticatedMember authenticatedMember = authenticationFacade.getCurrentMember();
        sessionTokenService.revokeRefreshToken(refreshToken);
        log.info("auth event=logout provider=local memberId={} ip={}", authenticatedMember.memberId(), clientIp);

        return new LogoutResult(true);
    }

    @Override
    @Transactional
    public LoginPayload exchangeOAuth2Code(OAuth2ExchangeCommand command, String clientIp) {
        if (command.provider() != SocialProviderType.GOOGLE) {
            throw new AuthenticationException(AuthErrorCode.OAUTH2_PROVIDER_NOT_SUPPORTED);
        }

        Member member = sessionTokenService.consumeExchangeCode(command.provider(), command.code());
        ensureActive(member);

        IssuedAccessToken issuedAccessToken = jwtTokenProvider.issueAccessToken(member);
        log.info(
                "auth event=oauth2_exchange_success provider={} memberId={} ip={}",
                command.provider().name().toLowerCase(),
                member.getId(),
                clientIp
        );

        return LoginPayload.from(issuedAccessToken, member);
    }

    private void ensureActive(Member member) {
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(MemberErrorCode.INACTIVE_MEMBER, member.getId());
        }
    }
}
