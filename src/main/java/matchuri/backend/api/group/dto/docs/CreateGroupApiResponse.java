package matchuri.backend.api.group.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.group.dto.response.CreateGroupResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "그룹 생성 API의 공통 응답 envelope입니다.")
public record CreateGroupApiResponse(boolean success, CreateGroupResponse data, ErrorResponse error) {
}
