package matchuri.backend.api.memberagreement.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.memberagreement.dto.response.RequiredAgreementStatusResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "필수 약관 상태 조회 API의 공통 응답 envelope입니다.")
public record RequiredAgreementStatusApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "성공 시 반환되는 필수 약관 상태 payload입니다.")
        RequiredAgreementStatusResponse data,

        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
