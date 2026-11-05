package matchuri.backend.api.memberagreement.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.memberagreement.dto.response.SubmitRequiredAgreementsResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "필수 약관 동의 제출 API의 공통 응답 envelope입니다.")
public record SubmitRequiredAgreementsApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "성공 시 반환되는 필수 약관 동의 처리 결과 payload입니다.")
        SubmitRequiredAgreementsResponse data,

        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
