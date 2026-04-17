package matchuri.backend.domain.member.result;

import java.util.List;
import matchuri.backend.domain.member.entity.AgreementType;

public record RequiredAgreementStatusResult(
        boolean requiredAgreementsCompleted,
        List<AgreementType> missingAgreementTypes
) {
}
