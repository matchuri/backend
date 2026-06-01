package matchuri.backend.domain.image.support;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;

@Component
public class ImageChecksumCalculator {

    public String sha256Hex(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(bytes);
            StringBuilder builder = new StringBuilder(hashedBytes.length * 2);

            for (byte hashedByte : hashedBytes) {
                builder.append(String.format("%02x", hashedByte));
            }

            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", exception);
        }
    }
}
