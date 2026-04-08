package matchuri.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.AuthErrorCode;
import matchuri.backend.domain.member.MemberErrorCode;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.exception.AuthenticationException;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuth2MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member findOrCreateMember(SocialProviderType provider, String providerUserId, String email, String nickname) {
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new AuthenticationException(AuthErrorCode.OAUTH2_PROVIDER_USERINFO_MISSING);
        }

        Member member = memberRepository.findBySocialProviderTypeAndSocialProviderUserId(provider, providerUserId)
                .orElseGet(() -> createSocialMember(provider, providerUserId, email, nickname));

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(MemberErrorCode.INACTIVE_MEMBER, MemberErrorCode.INACTIVE_MEMBER.format(member.getId()));
        }

        return member;
    }

    private Member createSocialMember(SocialProviderType provider, String providerUserId, String email, String nickname) {
        try {
            return memberRepository.saveAndFlush(Member.createSocialMember(provider, providerUserId, email, nickname));
        } catch (DataIntegrityViolationException exception) {
            return memberRepository.findBySocialProviderTypeAndSocialProviderUserId(provider, providerUserId)
                    .orElseThrow(() -> exception);
        }
    }
}
