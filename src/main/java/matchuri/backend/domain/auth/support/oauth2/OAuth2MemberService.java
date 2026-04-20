package matchuri.backend.domain.auth.support.oauth2;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.member.exception.MemberErrorCode;
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
    public Member findOrCreateMember(SocialProviderType provider, String providerUserId, String email) {
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new AuthenticationException(AuthErrorCode.OAUTH2_PROVIDER_USERINFO_MISSING);
        }

        Member member = memberRepository.findBySocialProviderTypeAndSocialProviderUserId(provider, providerUserId)
                .orElseGet(() -> createSocialMember(provider, providerUserId, email));

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(MemberErrorCode.INACTIVE_MEMBER, member.getId());
        }

        return member;
    }

    private Member createSocialMember(SocialProviderType provider, String providerUserId, String email) {
        String temporaryNickname = generateUniqueTemporaryNickname(provider, email);

        try {
            return memberRepository.saveAndFlush(Member.createSocialMember(provider, providerUserId, email, temporaryNickname));
        } catch (DataIntegrityViolationException exception) {
            return memberRepository.findBySocialProviderTypeAndSocialProviderUserId(provider, providerUserId)
                    .orElseThrow(() -> exception);
        }
    }

    private String generateUniqueTemporaryNickname(SocialProviderType provider, String email) {
        String baseNickname = buildBaseTemporaryNickname(provider, email);
        String candidate = baseNickname;
        int suffix = 1;

        while (memberRepository.existsByNickname(candidate)) {
            candidate = appendSuffix(baseNickname, suffix++);
        }

        return candidate;
    }

    private String buildBaseTemporaryNickname(SocialProviderType provider, String email) {
        String emailLocalPart = extractEmailLocalPart(email);
        String providerName = provider.toRegistrationId();
        String rawBase = emailLocalPart + "_" + providerName;

        if (rawBase.length() <= Member.NICKNAME_MAX_SIZE) {
            return rawBase;
        }

        int maxLocalPartLength = Math.max(1, Member.NICKNAME_MAX_SIZE - providerName.length() - 1);
        return emailLocalPart.substring(0, Math.min(emailLocalPart.length(), maxLocalPartLength)) + "_" + providerName;
    }

    private String appendSuffix(String baseNickname, int suffix) {
        String suffixValue = "_" + suffix;
        int maxBaseLength = Math.max(1, Member.NICKNAME_MAX_SIZE - suffixValue.length());
        String truncatedBase = baseNickname.substring(0, Math.min(baseNickname.length(), maxBaseLength));
        return truncatedBase + suffixValue;
    }

    private String extractEmailLocalPart(String email) {
        if (email == null || email.isBlank()) {
            return "user";
        }

        int separatorIndex = email.indexOf('@');
        if (separatorIndex <= 0) {
            return email.toLowerCase(Locale.ROOT);
        }

        return email.substring(0, separatorIndex).toLowerCase(Locale.ROOT);
    }
}
