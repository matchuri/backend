package matchuri.backend.infra.storage;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.image.exception.ImageErrorCode;
import matchuri.backend.global.config.R2Config;
import matchuri.backend.global.exception.BusinessException;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
public class R2ObjectStorageClient implements ObjectStorageClient {

    private final S3Client s3Client;
    private final R2Config r2Config;

    @Override
    public void upload(UploadObjectCommand command) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(r2Config.getBucket())
                    .key(command.objectKey())
                    .contentType(command.contentType())
                    .contentLength((long) command.content().length)
                    .cacheControl(r2Config.getCacheControl())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(command.content()));
        } catch (S3Exception exception) {
            throw new BusinessException(ImageErrorCode.STORAGE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(r2Config.getBucket())
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(request);
        } catch (S3Exception exception) {
            throw new BusinessException(ImageErrorCode.STORAGE_DELETE_FAILED);
        }
    }
}
