package matchuri.backend.api.memberagreement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record RequiredAgreementStatusResponse(
        @Schema(description = "현재 로그인한 회원이 필수 약관 2종의 최신 버전에 모두 동의했는지 여부입니다.", example = "false")
        boolean requiredAgreementsCompleted,

        @Schema(description = "아직 최신 필수 버전에 동의하지 않은 약관 종류 목록입니다.", example = "[\"TERMS_OF_SERVICE\", \"PRIVACY_POLICY\"]")
        List<String> missingAgreementTypes
) {
}
