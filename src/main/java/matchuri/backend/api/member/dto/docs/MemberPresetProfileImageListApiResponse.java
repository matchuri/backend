package matchuri.backend.api.member.dto.docs;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import matchuri.backend.api.member.dto.response.MemberPresetProfileImageResponse;
import matchuri.backend.global.api.ErrorResponse;

@Schema(description = "사용자용 프리셋 프로필 이미지 목록 API 공통 응답")
public record MemberPresetProfileImageListApiResponse(
        @Schema(description = "요청 성공 여부", example = "true")
        boolean success,

        @ArraySchema(schema = @Schema(implementation = MemberPresetProfileImageResponse.class))
        List<MemberPresetProfileImageResponse> data,

        @Schema(description = "실패 시 반환되는 에러 정보")
        ErrorResponse error
) {
}