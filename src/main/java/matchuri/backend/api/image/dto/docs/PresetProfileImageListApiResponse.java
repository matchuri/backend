package matchuri.backend.api.image.dto.docs;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import matchuri.backend.global.api.ErrorResponse;
import matchuri.backend.api.image.dto.response.PresetProfileImageResponse;

@Schema(description = "프리셋 프로필 이미지 목록 API 공통 응답")
public record PresetProfileImageListApiResponse(
        @Schema(description = "요청 성공 여부", example = "true")
        boolean success,

        @ArraySchema(schema = @Schema(implementation = PresetProfileImageResponse.class))
        List<PresetProfileImageResponse> data,

        @Schema(description = "실패 시 반환되는 에러 정보")
        ErrorResponse error
) {
}
