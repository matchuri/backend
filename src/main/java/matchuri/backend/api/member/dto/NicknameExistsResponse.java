package matchuri.backend.api.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record NicknameExistsResponse(
        @Schema(description = "중복 확인한 닉네임입니다.", example = "example_google")
        String nickname,

        @Schema(description = "이미 존재하는 닉네임인지 여부입니다. true면 이미 사용 중입니다.", example = "true")
        boolean exists
) {
}
