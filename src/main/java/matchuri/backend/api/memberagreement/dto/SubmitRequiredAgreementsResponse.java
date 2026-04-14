package matchuri.backend.api.memberagreement.dto;

import java.util.List;

public record SubmitRequiredAgreementsResponse(
        boolean requiredAgreementsCompleted,
        List<String> missingAgreementTypes,
        String accessToken,
        long expiresIn
) {
}
