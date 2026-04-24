package matchuri.backend.domain.auth.support.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class OAuth2MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private OAuth2MemberService oAuth2MemberService;

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

        when(memberRepository.findBySocialProviderTypeAndSocialProviderUserId(SocialProviderType.GOOGLE,
                providerUserId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingMember));
        when(memberRepository.existsByNickname("google_google")).thenReturn(false);
        when(memberRepository.saveAndFlush(any(Member.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate social member"));

        Member resolvedMember = oAuth2MemberService.findOrCreateMember(
                SocialProviderType.GOOGLE,
                providerUserId,
                "google@example.com"
        );

        assertThat(resolvedMember).isSameAs(existingMember);
        verify(memberRepository).saveAndFlush(any(Member.class));
    }

    @Test
    @DisplayName("신규 소셜 회원은 이메일 로컬파트와 provider로 임시 닉네임을 생성한다")
    void createsSocialMemberWithTemporaryNickname() {
        String providerUserId = "google-user-1";
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);

        when(memberRepository.findBySocialProviderTypeAndSocialProviderUserId(SocialProviderType.GOOGLE,
                providerUserId))
                .thenReturn(Optional.empty());
        when(memberRepository.existsByNickname("example_google")).thenReturn(false);
        when(memberRepository.saveAndFlush(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Member createdMember = oAuth2MemberService.findOrCreateMember(
                SocialProviderType.GOOGLE,
                providerUserId,
                "example@google.com"
        );

        verify(memberRepository).saveAndFlush(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getNickname()).isEqualTo("example_google");
        assertThat(createdMember.getNickname()).isEqualTo("example_google");
    }

    @Test
    @DisplayName("임시 닉네임이 이미 존재하면 숫자 suffix를 붙여 유니크하게 저장한다")
    void createsUniqueTemporaryNicknameWhenBaseNicknameAlreadyExists() {
        String providerUserId = "google-user-2";
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);

        when(memberRepository.findBySocialProviderTypeAndSocialProviderUserId(SocialProviderType.GOOGLE,
                providerUserId))
                .thenReturn(Optional.empty());
        when(memberRepository.existsByNickname("example_google")).thenReturn(true);
        when(memberRepository.existsByNickname("example_google_1")).thenReturn(false);
        when(memberRepository.saveAndFlush(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Member createdMember = oAuth2MemberService.findOrCreateMember(
                SocialProviderType.GOOGLE,
                providerUserId,
                "example@google.com"
        );

        verify(memberRepository).saveAndFlush(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getNickname()).isEqualTo("example_google_1");
        assertThat(createdMember.getNickname()).isEqualTo("example_google_1");
    }
}
