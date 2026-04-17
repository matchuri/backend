package matchuri.backend.domain.member.result;

import matchuri.backend.domain.auth.service.IssuedAccessToken;

public record SubmitRequiredAgreementsResult(
        RequiredAgreementStatusResult status,
        IssuedAccessToken issuedAccessToken
) {
}
