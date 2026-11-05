package matchuri.backend.api.group.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.group.dto.response.GroupDetailV2Response;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "그룹 상세 조회 v2 API의 공통 응답 envelope입니다.")
public record GroupDetailV2ApiResponse(boolean success, GroupDetailV2Response data, ErrorResponse error) {
}
