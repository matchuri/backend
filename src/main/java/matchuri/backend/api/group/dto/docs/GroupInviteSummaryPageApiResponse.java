package matchuri.backend.api.group.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.group.dto.response.GroupInviteSummaryResponse;
import matchuri.backend.global.api.ErrorResponse;
import matchuri.backend.global.api.PageResponse;

@Schema(description = "내 그룹 초대 목록 API의 공통 응답 envelope입니다.")
public record GroupInviteSummaryPageApiResponse(
        boolean success,
        PageResponse<GroupInviteSummaryResponse> data,
        ErrorResponse error
) {
}
