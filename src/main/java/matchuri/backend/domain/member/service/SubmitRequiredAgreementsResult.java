package matchuri.backend.domain.member.service;

import matchuri.backend.domain.auth.service.IssuedAccessToken;

public record SubmitRequiredAgreementsResult(
        RequiredAgreementStatusResult status,
        IssuedAccessToken issuedAccessToken
) {
}
