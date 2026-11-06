package matchuri.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.result.IssuedAccessToken;
import matchuri.backend.domain.auth.result.LoginPayload;
import matchuri.backend.domain.auth.support.token.JwtTokenProvider;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.support.member.MemberReader;
import matchuri.backend.domain.member.support.onboarding.OnboardingStatusResolver;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Profile("local & !dev & !prod")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalTestAuthService {

    private final MemberReader memberReader;
    private final JwtTokenProvider jwtTokenProvider;
    private final OnboardingStatusResolver onboardingStatusResolver;

    public LoginPayload login(Long memberId) {
        Member member = memberReader.getActiveMember(memberId);
        IssuedAccessToken issuedAccessToken = jwtTokenProvider.issueAccessToken(member);

        log.info("auth event=test_login_success memberId={}", member.getId());
        return LoginPayload.from(issuedAccessToken, member, onboardingStatusResolver.resolve(member));
    }
}
