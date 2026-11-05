package matchuri.backend.api.group.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.group.dto.response.GroupInviteLinkPreviewResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "그룹 초대 링크 미리보기 API의 공통 응답 envelope입니다.")
public record GroupInviteLinkPreviewApiResponse(
        boolean success,
        GroupInviteLinkPreviewResponse data,
        ErrorResponse error
) {
}
