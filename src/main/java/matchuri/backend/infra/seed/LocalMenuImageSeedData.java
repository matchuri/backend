package matchuri.backend.infra.seed;

import java.util.List;

public record LocalMenuImageSeedData(
        List<MenuImageSeed> menuImages
) {

    public record MenuImageSeed(
            String menuCode,
            String objectKey,
            String originalFilename,
            String contentType,
            long contentLength,
            String checksum,
            int width,
            int height
    ) {
    }
}
