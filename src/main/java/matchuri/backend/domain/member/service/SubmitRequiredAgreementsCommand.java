package matchuri.backend.domain.member.service;

import java.util.List;

public record SubmitRequiredAgreementsCommand(
        List<AgreementConsentCommand> agreements
) {

    public record AgreementConsentCommand(
            String agreementType,
            String agreementVersion
    ) {
    }
}
