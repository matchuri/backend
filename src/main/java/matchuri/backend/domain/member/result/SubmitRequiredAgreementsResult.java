package matchuri.backend.domain.member.result;

import matchuri.backend.domain.auth.result.IssuedAccessToken;

public record SubmitRequiredAgreementsResult(
        RequiredAgreementStatusResult status,
        IssuedAccessToken issuedAccessToken,
        OnboardingStatusResult onboarding
) {
}
