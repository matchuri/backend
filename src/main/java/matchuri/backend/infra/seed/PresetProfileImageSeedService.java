package matchuri.backend.infra.seed;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.image.entity.ImageAsset;
import matchuri.backend.domain.image.entity.ImageStorageProvider;
import matchuri.backend.domain.image.entity.PresetProfileImage;
import matchuri.backend.domain.image.repository.ImageAssetRepository;
import matchuri.backend.domain.image.repository.PresetProfileImageRepository;
import matchuri.backend.global.config.R2Config;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresetProfileImageSeedService {

    private static final String RESOURCE_PATH = "seed/preset-profile-images.json";

    private final SeedDataResourceLoader resourceLoader;
    private final ImageAssetRepository imageAssetRepository;
    private final PresetProfileImageRepository presetProfileImageRepository;
    private final R2Config r2Config;

    @Transactional
    public void initialize() {
        PresetProfileImageSeedData seedData =
                resourceLoader.load(RESOURCE_PATH, PresetProfileImageSeedData.class);
        List<PresetProfileImage> seededPresets = new ArrayList<>();

        for (PresetProfileImageSeedData.PresetProfileImageSeed seed : seedData.presetProfileImages()) {
            ImageAsset asset = imageAssetRepository.findByObjectKey(seed.objectKey())
                    .orElseGet(() -> imageAssetRepository.save(new ImageAsset(
                            ImageStorageProvider.CLOUDFLARE_R2,
                            r2Config.getBucket(),
                            seed.objectKey(),
                            seed.originalFilename(),
                            seed.contentType(),
                            seed.contentLength(),
                            seed.checksum(),
                            seed.width(),
                            seed.height()
                    )));
            PresetProfileImage preset = presetProfileImageRepository.findByObjectKey(seed.objectKey())
                    .orElseGet(() -> presetProfileImageRepository.save(new PresetProfileImage(asset, false)));
            seededPresets.add(preset);
        }

        ensureSingleDefault(seedData, seededPresets);

        log.info("Preset profile image seed initialization completed. presets={}", seededPresets.size());
    }

    private void ensureSingleDefault(
            PresetProfileImageSeedData seedData,
            List<PresetProfileImage> seededPresets
    ) {
        List<PresetProfileImage> currentDefaults = presetProfileImageRepository.findActiveDefaults();
        if (currentDefaults.size() > 1) {
            throw new IllegalStateException("활성 기본 프리셋 프로필 이미지가 둘 이상입니다.");
        }
        if (!currentDefaults.isEmpty()) {
            return;
        }

        for (int index = 0; index < seedData.presetProfileImages().size(); index++) {
            if (seedData.presetProfileImages().get(index).isDefault()
                    && !seededPresets.get(index).isDeleted()) {
                seededPresets.get(index).setDefault();
                return;
            }
        }

        throw new IllegalStateException("기본 프리셋 프로필 이미지 seed가 없습니다.");
    }
}
