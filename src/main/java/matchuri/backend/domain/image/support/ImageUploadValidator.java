package matchuri.backend.domain.image.support;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.image.exception.ImageErrorCode;
import matchuri.backend.global.config.R2Config;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ImageUploadValidator {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final R2Config r2Config;

    public ValidatedImage validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ImageErrorCode.UPLOAD_FILE_EMPTY);
        }

        if (file.getSize() > r2Config.getMaxUploadBytes()) {
            throw new BusinessException(ImageErrorCode.UPLOAD_FILE_TOO_LARGE, r2Config.getMaxUploadBytes());
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ImageErrorCode.UNSUPPORTED_CONTENT_TYPE, contentType);
        }

        byte[] bytes = toBytes(file);
        ImageDimensions dimensions = dimensions(contentType, bytes);
        validateResolution(dimensions);

        return new ValidatedImage(
                contentType,
                bytes,
                dimensions.width(),
                dimensions.height()
        );
    }

    private byte[] toBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BusinessException(ImageErrorCode.INVALID_CONTENT);
        }
    }

    private ImageDimensions dimensions(String contentType, byte[] bytes) {
        if ("image/webp".equals(contentType)) {
            return webpDimensions(bytes);
        }

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new BusinessException(ImageErrorCode.INVALID_CONTENT);
            }

            return new ImageDimensions(image.getWidth(), image.getHeight());
        } catch (IOException exception) {
            throw new BusinessException(ImageErrorCode.INVALID_CONTENT);
        }
    }

    private ImageDimensions webpDimensions(byte[] bytes) {
        if (bytes.length < 30
                || !asciiEquals(bytes, 0, "RIFF")
                || !asciiEquals(bytes, 8, "WEBP")) {
            throw new BusinessException(ImageErrorCode.INVALID_CONTENT);
        }

        int offset = 12;
        while (offset + 8 <= bytes.length) {
            String chunkType = ascii(bytes, offset, 4);
            int chunkSize = littleEndianInt(bytes, offset + 4);
            int dataOffset = offset + 8;

            if (dataOffset + chunkSize > bytes.length) {
                throw new BusinessException(ImageErrorCode.INVALID_CONTENT);
            }

            if ("VP8X".equals(chunkType)) {
                if (chunkSize < 10) {
                    throw new BusinessException(ImageErrorCode.INVALID_CONTENT);
                }
                int width = 1 + littleEndian24(bytes, dataOffset + 4);
                int height = 1 + littleEndian24(bytes, dataOffset + 7);
                return new ImageDimensions(width, height);
            }

            if ("VP8 ".equals(chunkType)) {
                if (chunkSize < 10
                        || bytes[dataOffset + 3] != (byte) 0x9d
                        || bytes[dataOffset + 4] != 0x01
                        || bytes[dataOffset + 5] != 0x2a) {
                    throw new BusinessException(ImageErrorCode.INVALID_CONTENT);
                }
                int width = littleEndianShort(bytes, dataOffset + 6) & 0x3fff;
                int height = littleEndianShort(bytes, dataOffset + 8) & 0x3fff;
                return new ImageDimensions(width, height);
            }

            if ("VP8L".equals(chunkType)) {
                if (chunkSize < 5 || bytes[dataOffset] != 0x2f) {
                    throw new BusinessException(ImageErrorCode.INVALID_CONTENT);
                }
                int packed = littleEndianInt(bytes, dataOffset + 1);
                int width = 1 + (packed & 0x3fff);
                int height = 1 + ((packed >> 14) & 0x3fff);
                return new ImageDimensions(width, height);
            }

            offset = dataOffset + chunkSize + (chunkSize % 2);
        }

        throw new BusinessException(ImageErrorCode.INVALID_CONTENT);
    }

    private void validateResolution(ImageDimensions dimensions) {
        if (dimensions.width() < r2Config.getMinImageWidth()
                || dimensions.height() < r2Config.getMinImageHeight()
                || dimensions.width() > r2Config.getMaxImageWidth()
                || dimensions.height() > r2Config.getMaxImageHeight()) {
            throw new BusinessException(ImageErrorCode.INVALID_RESOLUTION, dimensions.width(), dimensions.height());
        }
    }

    private boolean asciiEquals(byte[] bytes, int offset, String expected) {
        return ascii(bytes, offset, expected.length()).equals(expected);
    }

    private String ascii(byte[] bytes, int offset, int length) {
        if (offset + length > bytes.length) {
            throw new BusinessException(ImageErrorCode.INVALID_CONTENT);
        }

        return new String(bytes, offset, length, java.nio.charset.StandardCharsets.US_ASCII);
    }

    private int littleEndian24(byte[] bytes, int offset) {
        return Byte.toUnsignedInt(bytes[offset])
                | (Byte.toUnsignedInt(bytes[offset + 1]) << 8)
                | (Byte.toUnsignedInt(bytes[offset + 2]) << 16);
    }

    private int littleEndianShort(byte[] bytes, int offset) {
        return Byte.toUnsignedInt(bytes[offset])
                | (Byte.toUnsignedInt(bytes[offset + 1]) << 8);
    }

    private int littleEndianInt(byte[] bytes, int offset) {
        return Byte.toUnsignedInt(bytes[offset])
                | (Byte.toUnsignedInt(bytes[offset + 1]) << 8)
                | (Byte.toUnsignedInt(bytes[offset + 2]) << 16)
                | (Byte.toUnsignedInt(bytes[offset + 3]) << 24);
    }

    private record ImageDimensions(int width, int height) {
    }

    public record ValidatedImage(
            String contentType,
            byte[] bytes,
            int width,
            int height
    ) {
    }
}
