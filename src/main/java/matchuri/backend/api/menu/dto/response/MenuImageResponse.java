package matchuri.backend.api.menu.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import matchuri.backend.domain.menu.result.MenuImageResult;

public record MenuImageResponse(
        @Schema(description = "메뉴 ID입니다.", example = "1001")
        Long menuId,

        @Schema(description = "이미지 자산 ID입니다.", example = "2001")
        Long imageAssetId,

        @Schema(description = "메뉴 이미지 URL입니다.", example = "https://asset.matchuri.com/menu-items/1001/sample.jpg")
        String thumbnailUrl,

        @Schema(description = "R2 object key입니다.", example = "menu-items/1001/sample.jpg")
        String objectKey,

        @Schema(description = "콘텐츠 타입입니다.", example = "image/jpeg")
        String contentType,

        @Schema(description = "콘텐츠 크기입니다.", example = "152000")
        Long contentLength,

        @Schema(description = "이미지 너비입니다.", example = "1200")
        Integer width,

        @Schema(description = "이미지 높이입니다.", example = "900")
        Integer height
) {
    public static MenuImageResponse from(MenuImageResult result) {
        return new MenuImageResponse(
                result.menuId(),
                result.imageAssetId(),
                result.thumbnailUrl(),
                result.objectKey(),
                result.contentType(),
                result.contentLength(),
                result.width(),
                result.height()
        );
    }
}
