package matchuri.backend.infra.seed;

import java.util.List;

public record PresetProfileImageSeedData(
        List<PresetProfileImageSeed> presetProfileImages
) {

    public record PresetProfileImageSeed(
            String objectKey,
            String originalFilename,
            String contentType,
            long contentLength,
            String checksum,
            int width,
            int height,
            boolean isDefault
    ) {
    }
}
