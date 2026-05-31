package matchuri.backend.infra;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.config.R2Config;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class ImageUploadService {
    private static final Set<String> ALLOWED = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final S3Client s3Client;
    private final R2Config r2Config;

    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String type = file.getContentType();
        if(type==null ||! ALLOWED.contains(type)){
            throw new IllegalArgumentException("File type is not allowed");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename,type);
        String key = "uploads/"+ UUID.randomUUID()+extension;

        PutObjectRequest req = PutObjectRequest.builder().bucket(r2Config.getBucket()).key(key).contentType(type).build();

        s3Client.putObject(req, RequestBody.fromBytes(file.getBytes()));

        return r2Config.getPublicUrl()+"/"+key;

    }

    private String extractExtension(String originalFileName,String type){
        if (originalFileName != null && originalFileName.contains(".")) {
            return originalFileName.substring(originalFileName.lastIndexOf('.')).toLowerCase();
        }
        // 파일명이 없거나 이상하면 content-type으로 대충 보정
        return switch (type) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> "";
        };
    }
}