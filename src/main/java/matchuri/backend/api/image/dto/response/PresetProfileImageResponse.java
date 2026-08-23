package matchuri.backend.api.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.domain.image.result.PresetProfileImageResult;

public record PresetProfileImageResponse(
        @Schema(description = "프리셋 프로필 이미지 ID", example = "1")
        Long id,

        @Schema(description = "이미지 에셋 ID", example = "10")
        Long imageAssetId,

        @Schema(
                description = "공개 이미지 URL",
                example = "https://asset.matchuri.com/preset-profile/v1-spaghetti.png"
        )
        String imageUrl,

        @Schema(description = "R2 object key", example = "preset-profile/v1-spaghetti.png")
        String objectKey,

        @Schema(description = "업로드 원본 파일명", example = "v1-spaghetti.png", nullable = true)
        String originalFilename,

        @Schema(description = "MIME type", example = "image/png")
        String contentType,

        @Schema(description = "파일 크기(byte)", example = "628206")
        long contentLength,

        @Schema(description = "이미지 너비", example = "1254")
        int width,

        @Schema(description = "이미지 높이", example = "1254")
        int height,

        @Schema(description = "기본 프리셋 여부", example = "true")
        boolean isDefault
) {

    public static PresetProfileImageResponse from(PresetProfileImageResult result) {
        return new PresetProfileImageResponse(
                result.id(),
                result.imageAssetId(),
                result.imageUrl(),
                result.objectKey(),
                result.originalFilename(),
                result.contentType(),
                result.contentLength(),
                result.width(),
                result.height(),
                result.isDefault()
        );
    }
}
