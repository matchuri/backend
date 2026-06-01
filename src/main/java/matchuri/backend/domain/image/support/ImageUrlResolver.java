package matchuri.backend.domain.image.support;

import lombok.RequiredArgsConstructor;
import matchuri.backend.global.config.R2Config;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageUrlResolver {

    private final R2Config r2Config;

    public String toPublicUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        String baseUrl = r2Config.getPublicUrl();
        if (baseUrl.endsWith("/")) {
            return baseUrl + objectKey;
        }

        return baseUrl + "/" + objectKey;
    }
}
