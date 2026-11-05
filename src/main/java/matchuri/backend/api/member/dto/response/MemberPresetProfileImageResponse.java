package matchuri.backend.api.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberPresetProfileImageResponse(
        @Schema(description = "프리셋 프로필 이미지 ID", example = "1")
        Long presetProfileImageId,

        @Schema(
                description = "공개 이미지 URL",
                example = "https://asset.matchuri.com/preset-profile/v1-spaghetti.png"
        )
        String imageUrl,

        @Schema(description = "기본 프리셋 여부", example = "true")
        boolean isDefault
) {
}