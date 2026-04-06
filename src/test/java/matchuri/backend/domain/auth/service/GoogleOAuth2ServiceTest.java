package matchuri.backend.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class GoogleOAuth2ServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private GoogleOAuth2Service googleOAuth2Service;

    @Test
    @DisplayName("동시 생성 충돌이 발생하면 기존 구글 회원을 재조회해 반환한다")
    void returnsExistingMemberWhenConcurrentInsertCausesUniqueConstraintViolation() {
        String providerUserId = "google-user-1";
        Member existingMember = new Member(
                null,
                null,
                "google@example.com",
                true,
                SocialProviderType.GOOGLE,
                providerUserId,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        );

        when(memberRepository.findBySocialProviderTypeAndSocialProviderUserId(SocialProviderType.GOOGLE, providerUserId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingMember));
        when(memberRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(Member.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate social member"));

        Member resolvedMember = googleOAuth2Service.findOrCreateMember(providerUserId, "google@example.com", "구글사용자");

        assertThat(resolvedMember).isSameAs(existingMember);
        verify(memberRepository).saveAndFlush(org.mockito.ArgumentMatchers.any(Member.class));
    }
}
