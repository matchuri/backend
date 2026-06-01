package matchuri.backend.domain.menu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.image.entity.ImageAsset;
import matchuri.backend.domain.image.entity.ImageStorageProvider;
import matchuri.backend.domain.image.repository.ImageAssetRepository;
import matchuri.backend.domain.image.support.ImageChecksumCalculator;
import matchuri.backend.domain.image.support.ImageObjectKeyGenerator;
import matchuri.backend.domain.image.support.ImageUploadValidator;
import matchuri.backend.domain.image.support.ImageUploadValidator.ValidatedImage;
import matchuri.backend.domain.image.support.ImageUrlResolver;
import matchuri.backend.domain.menu.MenuErrorCode;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.entity.MenuItemImage;
import matchuri.backend.domain.menu.repository.MenuItemImageRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import matchuri.backend.domain.menu.result.AdminMenuItemDetailResult;
import matchuri.backend.domain.menu.result.MenuImageResult;
import matchuri.backend.global.config.R2Config;
import matchuri.backend.global.exception.BusinessException;
import matchuri.backend.infra.storage.ObjectStorageClient;
import matchuri.backend.infra.storage.UploadObjectCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MenuImageAdminServiceImpl implements MenuImageAdminService {

    private final MenuItemRepository menuItemRepository;
    private final MenuItemImageRepository menuItemImageRepository;
    private final ImageAssetRepository imageAssetRepository;
    private final ImageUploadValidator imageUploadValidator;
    private final ImageObjectKeyGenerator imageObjectKeyGenerator;
    private final ImageChecksumCalculator imageChecksumCalculator;
    private final ImageUrlResolver imageUrlResolver;
    private final ObjectStorageClient objectStorageClient;
    private final R2Config r2Config;

    @Override
    @Transactional(readOnly = true)
    public AdminMenuItemDetailResult getAdminMenuItemDetail(Long menuItemId) {
        MenuItem menuItem = getMenuItem(menuItemId);
        String thumbnailUrl = menuItemImageRepository.findByMenuId(menuItemId)
                .map(MenuItemImage::getImageAsset)
                .map(ImageAsset::getObjectKey)
                .map(imageUrlResolver::toPublicUrl)
                .orElse(null);

        return new AdminMenuItemDetailResult(
                menuItem.getId(),
                menuItem.getCode(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.isActive(),
                thumbnailUrl
        );
    }

    @Override
    public MenuImageResult uploadPrimaryImage(Long menuItemId, MultipartFile file) {
        MenuItem menuItem = getMenuItem(menuItemId);
        ValidatedImage image = imageUploadValidator.validate(file);
        String objectKey = imageObjectKeyGenerator.menuImageKey(menuItemId, image.contentType());

        objectStorageClient.upload(new UploadObjectCommand(objectKey, image.contentType(), image.bytes()));

        try {
            ImageAsset imageAsset = imageAssetRepository.save(new ImageAsset(
                    ImageStorageProvider.CLOUDFLARE_R2,
                    r2Config.getBucket(),
                    objectKey,
                    normalizeOriginalFilename(file.getOriginalFilename()),
                    image.contentType(),
                    image.bytes().length,
                    imageChecksumCalculator.sha256Hex(image.bytes()),
                    image.width(),
                    image.height()
            ));

            MenuItemImage menuItemImage = menuItemImageRepository.findByMenuId(menuItemId)
                    .map(existingImage -> replaceImage(existingImage, imageAsset))
                    .orElseGet(() -> menuItemImageRepository.save(new MenuItemImage(menuItem, imageAsset)));

            return toResult(menuItemImage);
        } catch (RuntimeException exception) {
            deleteUploadedObjectAfterDbFailure(objectKey, menuItemId);
            throw exception;
        }
    }

    @Override
    public void deletePrimaryImage(Long menuItemId) {
        getMenuItem(menuItemId);
        MenuItemImage menuItemImage = menuItemImageRepository.findByMenuId(menuItemId)
                .orElse(null);

        if (menuItemImage == null) {
            return;
        }

        ImageAsset imageAsset = menuItemImage.getImageAsset();
        String objectKey = imageAsset.getObjectKey();
        menuItemImageRepository.delete(menuItemImage);
        imageAsset.markDeleted();

        deleteObjectAfterCommit(objectKey, menuItemId);
    }

    private MenuItem getMenuItem(Long menuItemId) {
        return menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new BusinessException(MenuErrorCode.NOT_FOUND, menuItemId));
    }

    private MenuItemImage replaceImage(MenuItemImage existingImage, ImageAsset newImageAsset) {
        ImageAsset oldImageAsset = existingImage.getImageAsset();
        String oldObjectKey = oldImageAsset.getObjectKey();
        oldImageAsset.markDeleted();
        existingImage.replaceImageAsset(newImageAsset);

        deleteObjectAfterCommit(oldObjectKey, existingImage.getMenu().getId());
        return existingImage;
    }

    private MenuImageResult toResult(MenuItemImage menuItemImage) {
        ImageAsset imageAsset = menuItemImage.getImageAsset();

        return new MenuImageResult(
                menuItemImage.getMenu().getId(),
                imageAsset.getId(),
                imageUrlResolver.toPublicUrl(imageAsset.getObjectKey()),
                imageAsset.getObjectKey(),
                imageAsset.getContentType(),
                imageAsset.getContentLength(),
                imageAsset.getWidth(),
                imageAsset.getHeight()
        );
    }

    private String normalizeOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return null;
        }

        String trimmed = originalFilename.trim();
        if (trimmed.length() <= ImageAsset.ORIGINAL_FILENAME_MAX_LENGTH) {
            return trimmed;
        }

        return trimmed.substring(0, ImageAsset.ORIGINAL_FILENAME_MAX_LENGTH);
    }

    private void deleteUploadedObjectAfterDbFailure(String objectKey, Long menuItemId) {
        try {
            objectStorageClient.delete(objectKey);
        } catch (RuntimeException deleteException) {
            log.warn("Failed to delete uploaded menu image after DB failure. menuItemId={}, objectKey={}",
                    menuItemId, objectKey, deleteException);
        }
    }

    private void deleteObjectAfterCommit(String objectKey, Long menuItemId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    objectStorageClient.delete(objectKey);
                } catch (RuntimeException exception) {
                    log.warn("Failed to delete replaced menu image object. menuItemId={}, objectKey={}",
                            menuItemId, objectKey, exception);
                }
            }
        });
    }
}
