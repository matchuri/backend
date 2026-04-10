package matchuri.backend.api.memberagreement.dto;

import java.util.List;

public record RequiredAgreementStatusResponse(
        boolean requiredAgreementsCompleted,
        List<String> missingAgreementTypes
) {
}
