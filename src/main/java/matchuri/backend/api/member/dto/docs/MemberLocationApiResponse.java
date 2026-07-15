package matchuri.backend.api.member.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.member.dto.response.MemberLocationResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "내 개인 위치 API의 공통 응답 envelope입니다.")
public record MemberLocationApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,
        @Schema(description = "저장된 개인 위치입니다. 위치가 등록되지 않았으면 null입니다.", nullable = true)
        MemberLocationResponse data,
        @Schema(description = "실패 시 반환되는 에러 정보입니다.")
        ErrorResponse error
) {
}
