package matchuri.backend.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import matchuri.backend.domain.member.command.CreateMemberCommand;
import matchuri.backend.domain.member.command.RegisterLocalMemberCommand;
import matchuri.backend.domain.member.command.SubmitRequiredAgreementsCommand;
import matchuri.backend.domain.member.command.UpdateMemberBasicInfoCommand;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberAgreement;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.exception.MemberErrorCode;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import matchuri.backend.domain.member.result.RegisterLocalMemberResult;
import matchuri.backend.domain.member.result.UpdateMemberResult;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementRequestValidator;
import matchuri.backend.domain.member.support.member.ActiveMemberReader;
import matchuri.backend.global.exception.BusinessException;
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
    private MemberAgreementRepository memberAgreementRepository;

    @Mock
    private MemberTasteProfileRepository memberTasteProfileRepository;

    @Mock
    private RequiredAgreementRequestValidator requiredAgreementRequestValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ActiveMemberReader activeMemberReader;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    @DisplayName("자체 회원가입 통합은 회원과 필수 약관을 함께 저장한다")
    void registerLocalMemberSavesMemberAndRequiredAgreements() {
        RegisterLocalMemberCommand command = new RegisterLocalMemberCommand(
                "tester01",
                "P@ssw0rd!",
                "점심탐험가",
                List.of(
                        new SubmitRequiredAgreementsCommand.AgreementConsentCommand("TERMS_OF_SERVICE", "2026-04-10"),
                        new SubmitRequiredAgreementsCommand.AgreementConsentCommand("PRIVACY_POLICY", "2026-04-10")
                )
        );

        Member savedMember = Member.builder()
                .id(1L)
                .loginId("tester01")
                .passwordHash("encoded-password")
                .nickname("점심탐험가")
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .social(false)
                .build();

        when(memberRepository.existsByNickname("점심탐험가")).thenReturn(false);
        when(passwordEncoder.encode("P@ssw0rd!")).thenReturn("encoded-password");
        when(memberRepository.saveAndFlush(any(Member.class))).thenReturn(savedMember);
        when(requiredAgreementRequestValidator.validateAndIndex(any())).thenReturn(Map.of(
                AgreementType.TERMS_OF_SERVICE, "2026-04-10",
                AgreementType.PRIVACY_POLICY, "2026-04-10"
        ));

        RegisterLocalMemberResult result = memberService.registerLocalMember(command);

        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.loginId()).isEqualTo("tester01");
        assertThat(result.nickname()).isEqualTo("점심탐험가");
        verify(memberRepository).saveAndFlush(any(Member.class));
        verify(requiredAgreementRequestValidator).validateAndIndex(command.agreements());
        verify(memberAgreementRepository, times(2)).save(any(MemberAgreement.class));
    }

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

        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);
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

        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);

        UpdateMemberResult result = memberService.updateMyProfile(new UpdateMemberBasicInfoCommand("현재닉네임"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(member.getNickname()).isEqualTo("현재닉네임");
        verify(memberRepository).flush();
    }
}
