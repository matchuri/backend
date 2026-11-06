package matchuri.backend.api.group.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.group.dto.response.LeaveGroupResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "그룹 탈퇴 API의 공통 응답 envelope입니다.")
public record LeaveGroupApiResponse(boolean success, LeaveGroupResponse data, ErrorResponse error) {
}
