package matchuri.backend.domain.member.support.onboarding;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.result.OnboardingStatusResult;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementRevisionResolver;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OnboardingStatusResolver {

    private final RequiredAgreementRevisionResolver requiredAgreementRevisionResolver;

    public OnboardingStatusResult resolve(Member member) {
        boolean requiredAgreementsCompleted = requiredAgreementRevisionResolver
                .calculateStatus(member.getId())
                .requiredAgreementsCompleted();

        return OnboardingStatusResult.of(requiredAgreementsCompleted, member.isNicknameCompleted());
    }
}
