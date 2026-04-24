package matchuri.backend.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;
import matchuri.backend.domain.auth.result.IssuedAccessToken;
import matchuri.backend.domain.auth.support.token.JwtTokenProvider;
import matchuri.backend.domain.member.command.SubmitRequiredAgreementsCommand;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.exception.MemberAgreementErrorCode;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
import matchuri.backend.domain.member.result.OnboardingNextStep;
import matchuri.backend.domain.member.result.OnboardingStatusResult;
import matchuri.backend.domain.member.result.RequiredAgreementStatusResult;
import matchuri.backend.domain.member.result.SubmitRequiredAgreementsResult;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementRequestValidator;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementRevisionResolver;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementVersions;
import matchuri.backend.domain.member.support.member.ActiveMemberReader;
import matchuri.backend.domain.member.support.onboarding.OnboardingStatusResolver;
import matchuri.backend.global.exception.BusinessException;
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
    private ActiveMemberReader activeMemberReader;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RequiredAgreementRevisionResolver requiredAgreementRevisionResolver;

    @Mock
    private OnboardingStatusResolver onboardingStatusResolver;

    @Spy
    private RequiredAgreementRequestValidator requiredAgreementRequestValidator;

    @InjectMocks
    private MemberAgreementServiceImpl memberAgreementService;

    @Test
    @DisplayName("필수 약관 중 일부가 빠지면 MEMBER_AGREEMENT_REQUIRED_TYPES_MISSING을 반환한다")
    void submitRequiredAgreementsFailsWhenRequiredTypesMissing() {
        Member member = activeMember(1L);
        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);

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
        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);

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
        when(requiredAgreementRevisionResolver.calculateStatus(1L))
                .thenReturn(new RequiredAgreementStatusResult(true, List.of()));

        assertThat(memberAgreementService.hasCompletedRequiredAgreements(1L)).isTrue();
    }

    @Test
    @DisplayName("필수 약관 동의 제출 후 현재 revision으로 access token을 재발급한다")
    void submitRequiredAgreementsIssuesAccessTokenWithCurrentRevision() {
        Member member = activeMember(1L);
        when(activeMemberReader.getCurrentAuthenticatedActiveMember()).thenReturn(member);
        when(memberAgreementRepository.existsByMemberIdAndAgreementTypeAndAgreementVersion(anyLong(), any(), any()))
                .thenReturn(false);
        when(jwtTokenProvider.issueAccessToken(member, RequiredAgreementVersions.currentRevision()))
                .thenReturn(new IssuedAccessToken("new-access-token", 3600));
        when(requiredAgreementRevisionResolver.calculateStatus(1L))
                .thenReturn(new RequiredAgreementStatusResult(true, List.of()));
        when(onboardingStatusResolver.resolve(member))
                .thenReturn(new OnboardingStatusResult(true, true, true, OnboardingNextStep.READY));

        SubmitRequiredAgreementsCommand command = new SubmitRequiredAgreementsCommand(List.of(
                new SubmitRequiredAgreementsCommand.AgreementConsentCommand("TERMS_OF_SERVICE", "2026-04-10"),
                new SubmitRequiredAgreementsCommand.AgreementConsentCommand("PRIVACY_POLICY", "2026-04-10")
        ));

        SubmitRequiredAgreementsResult result = memberAgreementService.submitRequiredAgreements(command);

        assertThat(result.status().requiredAgreementsCompleted()).isTrue();
        assertThat(result.issuedAccessToken().accessToken()).isEqualTo("new-access-token");
        assertThat(result.issuedAccessToken().expiresIn()).isEqualTo(3600);
        assertThat(result.onboarding().completed()).isTrue();
    }

    @Test
    @DisplayName("이전 버전만 동의한 경우 필수 약관 완료가 아니다")
    void hasCompletedRequiredAgreementsReturnsFalseForOlderVersions() {
        when(requiredAgreementRevisionResolver.calculateStatus(1L))
                .thenReturn(new RequiredAgreementStatusResult(false, List.of(
                        AgreementType.TERMS_OF_SERVICE,
                        AgreementType.PRIVACY_POLICY
                )));

        assertThat(memberAgreementService.hasCompletedRequiredAgreements(1L)).isFalse();
    }

    @Test
    @DisplayName("과거 버전과 최신 버전 이력이 함께 있어도 최신 버전 존재 여부만으로 완료를 판단한다")
    void hasCompletedRequiredAgreementsIgnoresOlderAgreementHistory() {
        when(requiredAgreementRevisionResolver.calculateStatus(1L))
                .thenReturn(new RequiredAgreementStatusResult(true, List.of()));
        when(requiredAgreementRevisionResolver.resolve(1L))
                .thenReturn(RequiredAgreementVersions.currentRevision());

        assertThat(memberAgreementService.hasCompletedRequiredAgreements(1L)).isTrue();
        assertThat(requiredAgreementRevisionResolver.resolve(1L))
                .isEqualTo(RequiredAgreementVersions.currentRevision());
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
