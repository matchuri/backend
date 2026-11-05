package matchuri.backend.domain.image.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;

@Getter
@Entity
@Table(
        name = "image_assets",
        comment = "이미지 자산",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_image_assets_object_key", columnNames = "object_key")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageAsset extends BaseEntity {

    public static final int ORIGINAL_FILENAME_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "이미지 자산 ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider", nullable = false, length = 30, comment = "저장소 제공자")
    private ImageStorageProvider storageProvider;

    @Column(nullable = false, length = 100, comment = "버킷 이름")
    private String bucket;

    @Column(name = "object_key", nullable = false, length = 512, comment = "객체 키")
    private String objectKey;

    @Column(name = "original_filename", length = ORIGINAL_FILENAME_MAX_LENGTH, comment = "원본 파일명")
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 50, comment = "콘텐츠 타입")
    private String contentType;

    @Column(name = "content_length", nullable = false, comment = "콘텐츠 크기")
    private long contentLength;

    @Column(nullable = false, length = 64, comment = "SHA-256 체크섬")
    private String checksum;

    @Column(nullable = false, comment = "이미지 너비")
    private int width;

    @Column(nullable = false, comment = "이미지 높이")
    private int height;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "이미지 상태")
    private ImageAssetStatus status;

    public ImageAsset(
            ImageStorageProvider storageProvider,
            String bucket,
            String objectKey,
            String originalFilename,
            String contentType,
            long contentLength,
            String checksum,
            int width,
            int height
    ) {
        this.storageProvider = storageProvider;
        this.bucket = bucket;
        this.objectKey = objectKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.checksum = checksum;
        this.width = width;
        this.height = height;
        this.status = ImageAssetStatus.ACTIVE;
    }

    public void markDeleted() {
        this.status = ImageAssetStatus.DELETED;
    }
}
