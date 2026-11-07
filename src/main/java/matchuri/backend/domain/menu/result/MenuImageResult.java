package matchuri.backend.domain.menu.result;

public record MenuImageResult(
        Long menuId,
        Long imageAssetId,
        String thumbnailUrl,
        String objectKey,
        String contentType,
        long contentLength,
        int width,
        int height
) {
}
