package matchuri.backend.api.memberagreement;

import matchuri.backend.api.common.dto.OnboardingStatusResponse;
import matchuri.backend.api.memberagreement.dto.request.SubmitRequiredAgreementsRequest;
import matchuri.backend.api.memberagreement.dto.response.RequiredAgreementStatusResponse;
import matchuri.backend.api.memberagreement.dto.response.SubmitRequiredAgreementsResponse;
import matchuri.backend.domain.member.command.SubmitRequiredAgreementsCommand;
import matchuri.backend.domain.member.result.RequiredAgreementStatusResult;
import matchuri.backend.domain.member.result.SubmitRequiredAgreementsResult;
import org.springframework.stereotype.Component;

@Component
public class MemberAgreementMapper {

    public SubmitRequiredAgreementsCommand toCommand(SubmitRequiredAgreementsRequest request) {
        return new SubmitRequiredAgreementsCommand(
                request.agreements().stream()
                        .map(agreement -> new SubmitRequiredAgreementsCommand.AgreementConsentCommand(
                                agreement.agreementType(),
                                agreement.agreementVersion()
                        ))
                        .toList()
        );
    }

    public RequiredAgreementStatusResponse toResponse(RequiredAgreementStatusResult result) {
        return new RequiredAgreementStatusResponse(
                result.requiredAgreementsCompleted(),
                result.missingAgreementTypes().stream()
                        .map(Enum::name)
                        .toList()
        );
    }

    public SubmitRequiredAgreementsResponse toResponse(SubmitRequiredAgreementsResult result) {
        return new SubmitRequiredAgreementsResponse(
                result.status().requiredAgreementsCompleted(),
                result.status().missingAgreementTypes().stream()
                        .map(Enum::name)
                        .toList(),
                new OnboardingStatusResponse(
                        result.onboarding().requiredAgreementsCompleted(),
                        result.onboarding().nicknameCompleted(),
                        result.onboarding().completed(),
                        result.onboarding().nextStep()
                ),
                result.issuedAccessToken().accessToken(),
                result.issuedAccessToken().expiresIn()
        );
    }
}
