package matchuri.backend.api.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SetPresetProfileImageRequest(
        @NotNull
        @Positive
        @Schema(description = "설정할 활성 프리셋 프로필 이미지 ID", example = "1")
        Long presetProfileImageId
) {
}
