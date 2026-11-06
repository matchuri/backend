package matchuri.backend.api.group.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.group.dto.response.GroupInviteLinkResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "그룹 초대 링크 API의 공통 응답 envelope입니다.")
public record GroupInviteLinkApiResponse(boolean success, GroupInviteLinkResponse data, ErrorResponse error) {
}
