package matchuri.backend.api.group.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.group.dto.response.GroupInviteV2SummaryResponse;
import matchuri.backend.global.api.ErrorResponse;
import matchuri.backend.global.api.PageResponse;

@Schema(description = "v2 그룹 초대 목록 API의 공통 응답 envelope입니다.")
public record GroupInviteV2SummaryPageApiResponse(
        @Schema(description = "요청 성공 여부입니다.", example = "true")
        boolean success,

        @Schema(description = "성공 시 반환되는 그룹 초대 페이지입니다.")
        PageResponse<GroupInviteV2SummaryResponse> data,

        @Schema(description = "실패 정보입니다. 성공 시 null입니다.", nullable = true)
        ErrorResponse error
) {
}
