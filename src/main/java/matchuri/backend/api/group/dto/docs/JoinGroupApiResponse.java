package matchuri.backend.api.group.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.group.dto.response.JoinGroupResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "그룹 참여 API의 공통 응답 envelope입니다.")
public record JoinGroupApiResponse(boolean success, JoinGroupResponse data, ErrorResponse error) {
}
