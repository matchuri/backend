package matchuri.backend.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;
import matchuri.backend.domain.image.entity.ImageAsset;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@Table(
        name = "member_profile_images",
        comment = "회원 프로필 이미지",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_profile_images_member", columnNames = "member_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProfileImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "회원 프로필 이미지 ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, comment = "회원 ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_asset_id", nullable = false, comment = "이미지 에셋 ID")
    private ImageAsset imageAsset;

    public MemberProfileImage(Member member, ImageAsset imageAsset) {
        this.member = member;
        this.imageAsset = imageAsset;
    }

    public void replaceImageAsset(ImageAsset imageAsset) {
        this.imageAsset = imageAsset;
    }
}
