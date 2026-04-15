package matchuri.backend.api.memberagreement.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SubmitRequiredAgreementsRequest(
        @ArraySchema(
                schema = @Schema(implementation = AgreementConsentRequest.class),
                arraySchema = @Schema(description = "제출할 필수 약관 동의 목록입니다. 현재는 TERMS_OF_SERVICE, PRIVACY_POLICY 두 항목을 모두 포함해야 합니다.")
        )
        @NotEmpty(message = "agreements는 비어 있을 수 없습니다.")
        List<@Valid AgreementConsentRequest> agreements
) {

    public record AgreementConsentRequest(
            @Schema(description = "약관 종류입니다.", example = "TERMS_OF_SERVICE")
            @NotBlank(message = "agreementType은 비어 있을 수 없습니다.")
            String agreementType,

            @Schema(description = "동의한 약관 버전입니다. 현재 서버가 요구하는 최신 필수 버전과 일치해야 합니다.", example = "2026-04-10")
            @NotBlank(message = "agreementVersion은 비어 있을 수 없습니다.")
            String agreementVersion
    ) {
    }
}
