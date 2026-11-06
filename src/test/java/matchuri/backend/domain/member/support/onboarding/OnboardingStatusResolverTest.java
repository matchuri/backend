package matchuri.backend.domain.member.support.onboarding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import matchuri.backend.domain.member.entity.AgreementType;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import matchuri.backend.domain.member.result.OnboardingNextStep;
import matchuri.backend.domain.member.result.OnboardingStatusResult;
import matchuri.backend.domain.member.result.RequiredAgreementStatusResult;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementRevisionResolver;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OnboardingStatusResolverTest {

    @Mock
    private RequiredAgreementRevisionResolver requiredAgreementRevisionResolver;

    @Mock
    private MemberTasteProfileRepository memberTasteProfileRepository;

    @InjectMocks
    private OnboardingStatusResolver resolver;

    @ParameterizedTest(name = "agreements={0}, nickname={1}, tasteProfile={2} -> {3}")
    @CsvSource({
            "false, false, false, REQUIRED_AGREEMENTS",
            "false, false, true, REQUIRED_AGREEMENTS",
            "false, true, false, REQUIRED_AGREEMENTS",
            "false, true, true, REQUIRED_AGREEMENTS",
            "true, false, false, REQUIRED_NICKNAME",
            "true, false, true, REQUIRED_NICKNAME",
            "true, true, false, REQUIRED_TASTE_PROFILE",
            "true, true, true, READY"
    })
    void resolvesFirstIncompleteStep(boolean agreements, boolean nickname, boolean tasteProfile,
                                    OnboardingNextStep expectedStep) {
        Member member = Member.builder().id(1L).nicknameCompleted(nickname).build();
        when(requiredAgreementRevisionResolver.calculateStatus(1L))
                .thenReturn(new RequiredAgreementStatusResult(agreements,
                        agreements ? List.of() : List.of(AgreementType.TERMS_OF_SERVICE)));
        when(memberTasteProfileRepository.existsByMemberId(1L)).thenReturn(tasteProfile);

        OnboardingStatusResult result = resolver.resolve(member);

        assertThat(result.requiredAgreementsCompleted()).isEqualTo(agreements);
        assertThat(result.nicknameCompleted()).isEqualTo(nickname);
        assertThat(result.tasteProfileCompleted()).isEqualTo(tasteProfile);
        assertThat(result.nextStep()).isEqualTo(expectedStep);
        assertThat(result.completed()).isEqualTo(expectedStep == OnboardingNextStep.READY);
    }
}
