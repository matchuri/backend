package matchuri.backend.domain.image.support;

import java.util.UUID;
import matchuri.backend.domain.image.exception.ImageErrorCode;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class ImageObjectKeyGenerator {

    public String menuImageKey(Long menuItemId, String contentType) {
        return "menu-items/%d/%s%s".formatted(
                menuItemId,
                UUID.randomUUID(),
                extension(contentType)
        );
    }

    public String presetProfileImageKey(String contentType) {
        return "preset-profile/%s%s".formatted(UUID.randomUUID(), extension(contentType));
    }

    private String extension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new BusinessException(ImageErrorCode.UNSUPPORTED_CONTENT_TYPE, contentType);
        };
    }
}
