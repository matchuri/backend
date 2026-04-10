package matchuri.backend.api.memberagreement;

import matchuri.backend.api.memberagreement.dto.RequiredAgreementStatusResponse;
import matchuri.backend.api.memberagreement.dto.SubmitRequiredAgreementsRequest;
import matchuri.backend.domain.member.service.RequiredAgreementStatusResult;
import matchuri.backend.domain.member.service.SubmitRequiredAgreementsCommand;
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
}
