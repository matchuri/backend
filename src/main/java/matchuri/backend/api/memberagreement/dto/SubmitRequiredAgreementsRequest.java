package matchuri.backend.api.memberagreement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SubmitRequiredAgreementsRequest(
        @NotEmpty(message = "agreements는 비어 있을 수 없습니다.")
        List<@Valid AgreementConsentRequest> agreements
) {

    public record AgreementConsentRequest(
            @NotBlank(message = "agreementType은 비어 있을 수 없습니다.")
            String agreementType,
            @NotBlank(message = "agreementVersion은 비어 있을 수 없습니다.")
            String agreementVersion
    ) {
    }
}
