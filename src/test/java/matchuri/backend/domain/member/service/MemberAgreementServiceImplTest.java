package matchuri.backend.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.member.MemberAgreementErrorCode;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberAgreement;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.exception.BusinessException;
import matchuri.backend.global.security.AuthenticatedMember;
import matchuri.backend.global.security.AuthenticationFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberAgreementServiceImplTest {

    @Mock
    private MemberAgreementRepository memberAgreementRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Spy
    private RequiredAgreementRequestValidator requiredAgreementRequestValidator;

    @InjectMocks
    private MemberAgreementServiceImpl memberAgreementService;

    @Test
    @DisplayName("필수 약관 중 일부가 빠지면 MEMBER_AGREEMENT_REQUIRED_TYPES_MISSING을 반환한다")
    void submitRequiredAgreementsFailsWhenRequiredTypesMissing() {
        Member member = activeMember(1L);
        when(authenticationFacade.getCurrentMember()).thenReturn(new AuthenticatedMember(1L, "tester01", MemberRole.MEMBER));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        SubmitRequiredAgreementsCommand command = new SubmitRequiredAgreementsCommand(List.of(
                new SubmitRequiredAgreementsCommand.AgreementConsentCommand("TERMS_OF_SERVICE", "2026-04-10")
        ));

        assertThatThrownBy(() -> memberAgreementService.submitRequiredAgreements(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(MemberAgreementErrorCode.REQUIRED_TYPES_MISSING);
    }

    @Test
    @DisplayName("최신 필수 버전과 다르면 MEMBER_AGREEMENT_VERSION_MISMATCH를 반환한다")
    void submitRequiredAgreementsFailsWhenVersionMismatch() {
        Member member = activeMember(1L);
        when(authenticationFacade.getCurrentMember()).thenReturn(new AuthenticatedMember(1L, "tester01", MemberRole.MEMBER));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        SubmitRequiredAgreementsCommand command = new SubmitRequiredAgreementsCommand(List.of(
                new SubmitRequiredAgreementsCommand.AgreementConsentCommand("TERMS_OF_SERVICE", "2026-03-01"),
                new SubmitRequiredAgreementsCommand.AgreementConsentCommand("PRIVACY_POLICY", "2026-04-10")
        ));

        assertThatThrownBy(() -> memberAgreementService.submitRequiredAgreements(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(MemberAgreementErrorCode.VERSION_MISMATCH);
    }

    @Test
    @DisplayName("최신 필수 버전 이력이 모두 있으면 필수 약관 완료 상태를 반환한다")
    void hasCompletedRequiredAgreementsReturnsTrueWhenAllRequiredVersionsExist() {
        when(memberAgreementRepository.findByMemberIdAndAgreementTypeIn(1L, RequiredAgreementVersions.requiredTypes()))
                .thenReturn(List.of(
                        MemberAgreement.create(activeMember(1L), AgreementType.TERMS_OF_SERVICE, "2026-04-10"),
                        MemberAgreement.create(activeMember(1L), AgreementType.PRIVACY_POLICY, "2026-04-10")
                ));

        assertThat(memberAgreementService.hasCompletedRequiredAgreements(1L)).isTrue();
    }

    @Test
    @DisplayName("이전 버전만 동의한 경우 필수 약관 완료가 아니다")
    void hasCompletedRequiredAgreementsReturnsFalseForOlderVersions() {
        when(memberAgreementRepository.findByMemberIdAndAgreementTypeIn(1L, RequiredAgreementVersions.requiredTypes()))
                .thenReturn(List.of(
                        MemberAgreement.create(activeMember(1L), AgreementType.TERMS_OF_SERVICE, "2026-03-01"),
                        MemberAgreement.create(activeMember(1L), AgreementType.PRIVACY_POLICY, "2026-03-01")
                ));

        assertThat(memberAgreementService.hasCompletedRequiredAgreements(1L)).isFalse();
    }

    private Member activeMember(Long memberId) {
        return Member.builder()
                .id(memberId)
                .loginId("tester01")
                .passwordHash("hashed-password")
                .social(false)
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .build();
    }
}
