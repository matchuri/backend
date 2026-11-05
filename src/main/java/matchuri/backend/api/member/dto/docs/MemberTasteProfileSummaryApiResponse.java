package matchuri.backend.api.member.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.member.dto.response.MemberTasteProfileSummaryResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "내 취향 프로필 조회 API의 공통 응답 envelope입니다.")
public record MemberTasteProfileSummaryApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "성공 시 반환되는 내 취향 프로필 payload입니다.")
        MemberTasteProfileSummaryResponse data,

        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
