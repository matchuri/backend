package matchuri.backend.api.common.dto.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.api.common.dto.response.HomeResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "홈 조회 응답 envelope입니다.")
public record HomeApiResponse(boolean success, HomeResponse data, ErrorResponse error) {}
