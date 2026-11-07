package matchuri.backend.domain.image.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "preset_profile_images",
        comment = "프리셋 프로필 이미지"
)
public class PresetProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "프리셋 프로필 이미지 ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_asset_id", nullable = false, comment = "이미지 에셋 ID")
    private ImageAsset imageAsset;

    @Builder.Default
    @Column(name = "is_default", nullable = false, comment = "기본 프리셋 프로필 이미지 여부")
    private boolean isDefault = false;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false, comment = "삭제 여부")
    private boolean isDeleted = false;

    public PresetProfileImage(ImageAsset imageAsset, boolean isDefault) {
        this.imageAsset = imageAsset;
        this.isDefault = isDefault;
        this.isDeleted = false;
    }

    public void setDefault() {
        this.isDefault = true;
    }

    public void clearDefault() {
        this.isDefault = false;
    }

    public void delete() {
        this.isDeleted = true;
        this.isDefault = false;
    }
}
