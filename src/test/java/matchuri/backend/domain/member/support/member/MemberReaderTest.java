package matchuri.backend.domain.member.support.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.exception.MemberErrorCode;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberReaderTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberReader memberReader;

    @Test
    @DisplayName("memberId로 활성 회원을 조회한다")
    void getsActiveMemberById() {
        Member member = member(1L, MemberStatus.ACTIVE);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        assertThat(memberReader.getActiveMember(1L)).isSameAs(member);
    }

    @Test
    @DisplayName("존재하지 않는 memberId는 회원 없음으로 거절한다")
    void rejectsMissingMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberReader.getActiveMember(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("비활성 회원은 서비스 진입 전에 거절한다")
    void rejectsInactiveMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member(1L, MemberStatus.INACTIVE)));

        assertThatThrownBy(() -> memberReader.getActiveMember(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INACTIVE_MEMBER);
    }

    private Member member(Long memberId, MemberStatus status) {
        return Member.builder()
                .id(memberId)
                .loginId("member" + memberId)
                .memberRole(MemberRole.MEMBER)
                .status(status)
                .build();
    }
}
