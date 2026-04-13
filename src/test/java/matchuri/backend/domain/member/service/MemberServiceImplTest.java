package matchuri.backend.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import matchuri.backend.domain.member.MemberErrorCode;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import matchuri.backend.global.exception.BusinessException;
import matchuri.backend.global.security.AuthenticatedMember;
import matchuri.backend.global.security.AuthenticationFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberTasteProfileRepository memberTasteProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    @DisplayName("회원 가입 저장 충돌은 MEMBER_DUPLICATE_LOGIN_ID로 번역한다")
    void createMemberTranslatesIntegrityViolationToDuplicateLoginId() {
        CreateMemberCommand command = new CreateMemberCommand("tester01", "P@ssw0rd!");

        when(memberRepository.existsByLoginId("tester01")).thenReturn(false);
        when(passwordEncoder.encode("P@ssw0rd!")).thenReturn("encoded-password");
        when(memberRepository.saveAndFlush(any(Member.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate login id"));

        assertThatThrownBy(() -> memberService.createMember(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.DUPLICATE_LOGIN_ID);
    }

    @Test
    @DisplayName("내 닉네임 수정 시 이미 사용 중인 닉네임이면 MEMBER_DUPLICATE_NICKNAME을 반환한다")
    void updateMyProfileRejectsDuplicateNickname() {
        Member member = Member.builder()
                .id(1L)
                .loginId("tester01")
                .nickname("현재닉네임")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();

        when(authenticationFacade.getCurrentMember()).thenReturn(
                new AuthenticatedMember(1L, "tester01", MemberRole.MEMBER)
        );
        when(memberRepository.findById(1L)).thenReturn(java.util.Optional.of(member));
        when(memberRepository.existsByNickname("중복닉네임")).thenReturn(true);

        assertThatThrownBy(() -> memberService.updateMyProfile(new UpdateMemberBasicInfoCommand("중복닉네임")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.DUPLICATE_NICKNAME);
    }

    @Test
    @DisplayName("내 닉네임 수정 시 같은 닉네임이면 중복 검사 없이 유지한다")
    void updateMyProfileAllowsSameNickname() {
        Member member = Member.builder()
                .id(1L)
                .loginId("tester01")
                .nickname("현재닉네임")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();

        when(authenticationFacade.getCurrentMember()).thenReturn(
                new AuthenticatedMember(1L, "tester01", MemberRole.MEMBER)
        );
        when(memberRepository.findById(1L)).thenReturn(java.util.Optional.of(member));

        UpdateMemberResult result = memberService.updateMyProfile(new UpdateMemberBasicInfoCommand("현재닉네임"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(member.getNickname()).isEqualTo("현재닉네임");
        verify(memberRepository).flush();
    }
}
