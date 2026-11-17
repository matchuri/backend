package matchuri.backend.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import matchuri.backend.domain.auth.exception.AuthErrorCode;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.SocialProviderType;
import matchuri.backend.domain.member.support.member.MemberReader;
import matchuri.backend.global.exception.AuthenticationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OAuth2UnlinkerTest {

    @Mock
    private MemberReader memberReader;

    @Mock
    private OAuth2UnlinkClientFactory factory;

    @Mock
    private UnlinkClient client;

    @InjectMocks
    private OAuth2Unlinker unlinker;

    @Test
    @DisplayName("소셜 회원은 제공자에 맞는 클라이언트로 연결을 해제한다")
    void unlinksSocialMemberWithMatchingClient() {
        Member member = socialMember(1L, SocialProviderType.KAKAO);
        when(memberReader.get(1L)).thenReturn(member);
        when(factory.getClient(SocialProviderType.KAKAO)).thenReturn(Optional.of(client));

        unlinker.unlink(1L);

        verify(client).unlink(1L);
    }

    @Test
    @DisplayName("일반 회원은 제공자 클라이언트를 조회하지 않는다")
    void skipsLocalMember() {
        Member member = Member.builder().id(2L).memberRole(MemberRole.MEMBER).status(MemberStatus.DELETED).build();
        when(memberReader.get(2L)).thenReturn(member);

        unlinker.unlink(2L);

        verifyNoInteractions(factory);
        verify(client, never()).unlink(2L);
    }

    @Test
    @DisplayName("지원하지 않는 제공자는 OAUTH2_PROVIDER_NOT_SUPPORTED 예외를 던진다")
    void rejectsUnsupportedProvider() {
        Member member = socialMember(3L, SocialProviderType.GOOGLE);
        when(memberReader.get(3L)).thenReturn(member);
        when(factory.getClient(SocialProviderType.GOOGLE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unlinker.unlink(3L))
                .isInstanceOf(AuthenticationException.class)
                .satisfies(e -> assertThat(((AuthenticationException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.OAUTH2_PROVIDER_NOT_SUPPORTED));
    }

    private Member socialMember(Long id, SocialProviderType provider) {
        return Member.builder()
                .id(id)
                .social(true)
                .socialProviderType(provider)
                .socialProviderUserId("provider-user-" + id)
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.DELETED)
                .build();
    }
}
