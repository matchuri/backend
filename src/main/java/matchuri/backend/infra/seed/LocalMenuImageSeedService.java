package matchuri.backend.infra.seed;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.image.entity.ImageAsset;
import matchuri.backend.domain.image.entity.ImageStorageProvider;
import matchuri.backend.domain.image.repository.ImageAssetRepository;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.entity.MenuItemImage;
import matchuri.backend.domain.menu.repository.MenuItemImageRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import matchuri.backend.global.config.R2Config;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalMenuImageSeedService {

    private static final String RESOURCE_PATH = "seed/local-menu-images.json";

    private final SeedDataResourceLoader resourceLoader;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemImageRepository menuItemImageRepository;
    private final ImageAssetRepository imageAssetRepository;
    private final R2Config r2Config;

    @Transactional
    public void initialize() {
        LocalMenuImageSeedData seedData = resourceLoader.load(RESOURCE_PATH, LocalMenuImageSeedData.class);
        Map<String, MenuItem> menuItems = loadMenuItems();
        validateSeedData(seedData, menuItems);

        Map<Long, MenuItemImage> imagesByMenuId = new HashMap<>();
        Map<String, MenuItemImage> linksByObjectKey = new HashMap<>();
        menuItemImageRepository.findAll().forEach(menuItemImage -> {
            imagesByMenuId.put(menuItemImage.getMenu().getId(), menuItemImage);
            linksByObjectKey.put(menuItemImage.getImageAsset().getObjectKey(), menuItemImage);
        });

        int createdAssets = 0;
        int createdLinks = 0;
        for (LocalMenuImageSeedData.MenuImageSeed seed : seedData.menuImages()) {
            MenuItem menuItem = menuItems.get(seed.menuCode());
            if (imagesByMenuId.containsKey(menuItem.getId())) {
                continue;
            }
            if (linksByObjectKey.containsKey(seed.objectKey())) {
                throw new IllegalStateException(
                        "Local menu image seed의 object key가 다른 메뉴에 연결되어 있습니다. objectKey="
                                + seed.objectKey()
                );
            }

            Optional<ImageAsset> existingAsset = imageAssetRepository.findByObjectKey(seed.objectKey());
            ImageAsset imageAsset;
            if (existingAsset.isPresent()) {
                imageAsset = existingAsset.get();
            } else {
                imageAsset = imageAssetRepository.save(new ImageAsset(
                        ImageStorageProvider.CLOUDFLARE_R2,
                        r2Config.getBucket(),
                        seed.objectKey(),
                        seed.originalFilename(),
                        seed.contentType(),
                        seed.contentLength(),
                        seed.checksum(),
                        seed.width(),
                        seed.height()
                ));
                createdAssets++;
            }

            MenuItemImage menuItemImage = menuItemImageRepository.save(new MenuItemImage(menuItem, imageAsset));
            imagesByMenuId.put(menuItem.getId(), menuItemImage);
            linksByObjectKey.put(seed.objectKey(), menuItemImage);
            createdLinks++;
        }

        log.info(
                "Local menu image seed initialization completed. configured={}, createdAssets={}, createdLinks={}",
                seedData.menuImages().size(),
                createdAssets,
                createdLinks
        );
    }

    private Map<String, MenuItem> loadMenuItems() {
        Map<String, MenuItem> menuItems = new HashMap<>();
        menuItemRepository.findAll().forEach(menuItem -> {
            if (menuItems.put(menuItem.getCode(), menuItem) != null) {
                throw new IllegalStateException("DB menu item code가 중복되었습니다. code=" + menuItem.getCode());
            }
        });
        return menuItems;
    }

    private void validateSeedData(LocalMenuImageSeedData seedData, Map<String, MenuItem> menuItems) {
        Set<String> menuCodes = new HashSet<>();
        Set<String> objectKeys = new HashSet<>();
        for (LocalMenuImageSeedData.MenuImageSeed seed : seedData.menuImages()) {
            if (!menuCodes.add(seed.menuCode())) {
                throw new IllegalStateException(
                        "Local menu image seed의 menu code가 중복되었습니다. menuCode=" + seed.menuCode()
                );
            }
            if (!objectKeys.add(seed.objectKey())) {
                throw new IllegalStateException(
                        "Local menu image seed의 object key가 중복되었습니다. objectKey=" + seed.objectKey()
                );
            }
            if (!menuItems.containsKey(seed.menuCode())) {
                throw new IllegalStateException(
                        "Local menu image seed가 참조하는 menu item을 찾을 수 없습니다. menuCode="
                                + seed.menuCode()
                );
            }
        }
    }
}
