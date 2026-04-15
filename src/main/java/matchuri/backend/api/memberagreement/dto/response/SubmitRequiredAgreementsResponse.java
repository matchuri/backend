package matchuri.backend.api.memberagreement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SubmitRequiredAgreementsResponse(
        @Schema(description = "제출 후 현재 로그인한 회원의 필수 약관 완료 여부입니다.", example = "true")
        boolean requiredAgreementsCompleted,

        @Schema(description = "제출 후에도 누락된 약관 종류 목록입니다. 완료되면 빈 배열입니다.", example = "[]")
        List<String> missingAgreementTypes,

        @Schema(description = "필수 약관 revision이 반영된 새 access token입니다. 프론트는 성공 시 이 토큰으로 즉시 교체해야 합니다.", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "새 access token 만료까지 남은 시간(초)입니다.", example = "3600")
        long expiresIn
) {
}
