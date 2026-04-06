package matchuri.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.api.auth.dto.LoginRequest;
import matchuri.backend.api.auth.dto.LoginResponse;
import matchuri.backend.api.auth.dto.LogoutResponse;
import matchuri.backend.api.auth.dto.OAuth2ExchangeRequest;
import matchuri.backend.api.member.mapper.MemberMapper;
import matchuri.backend.domain.auth.AuthErrorCode;
import matchuri.backend.domain.member.MemberErrorCode;
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
    private final MemberMapper memberMapper;
    private final SessionTokenService sessionTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @Transactional
    public LoginResult login(LoginRequest request, String clientIp) {
        Member member = memberRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new AuthenticationException(AuthErrorCode.LOGIN_FAILED));

        ensureActive(member);

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new AuthenticationException(AuthErrorCode.LOGIN_FAILED);
        }

        TokenPair tokenPair = sessionTokenService.issueLoginTokenPair(member);
        log.info("auth event=login_success provider=local memberId={} ip={}", member.getId(), clientIp);

        return new LoginResult(
                memberMapper.toLoginResponse(member, tokenPair.accessToken(), tokenPair.accessTokenExpiresIn(), null),
                tokenPair.refreshToken()
        );
    }

    @Override
    @Transactional
    public LogoutResponse logout(String refreshToken, String clientIp) {
        AuthenticatedMember authenticatedMember = authenticationFacade.getCurrentMember();
        sessionTokenService.revokeRefreshToken(refreshToken);
        log.info("auth event=logout provider=local memberId={} ip={}", authenticatedMember.memberId(), clientIp);

        return new LogoutResponse(true);
    }

    @Override
    @Transactional
    public LoginResponse exchangeOAuth2Code(OAuth2ExchangeRequest request, String clientIp) {
        if (request.provider() != SocialProviderType.GOOGLE) {
            throw new AuthenticationException(AuthErrorCode.OAUTH2_PROVIDER_NOT_SUPPORTED);
        }

        Member member = sessionTokenService.consumeExchangeCode(request.provider(), request.code());
        ensureActive(member);

        IssuedAccessToken issuedAccessToken = jwtTokenProvider.issueAccessToken(member);
        log.info(
                "auth event=oauth2_exchange_success provider={} memberId={} ip={}",
                request.provider().name().toLowerCase(),
                member.getId(),
                clientIp
        );

        return memberMapper.toLoginResponse(member, issuedAccessToken.accessToken(), issuedAccessToken.expiresIn(), null);
    }

    private void ensureActive(Member member) {
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(
                    MemberErrorCode.INACTIVE_MEMBER,
                    MemberErrorCode.INACTIVE_MEMBER.format(member.getId())
            );
        }
    }
}
