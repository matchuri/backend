package matchuri.backend.domain.menu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Getter
@Entity
@Table(
        name = "menu_item_images",
        comment = "메뉴 이미지",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_menu_item_images_menu", columnNames = "menu_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuItemImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "메뉴 이미지 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private MenuItem menu;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_asset_id", nullable = false)
    private ImageAsset imageAsset;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_role", nullable = false, length = 20, comment = "이미지 역할")
    private MenuImageRole imageRole;

    @Column(name = "sort_order", nullable = false, comment = "정렬 순서")
    private int sortOrder;

    @Column(name = "is_primary", nullable = false, comment = "대표 이미지 여부")
    private boolean primary;

    public MenuItemImage(MenuItem menu, ImageAsset imageAsset) {
        this.menu = menu;
        this.imageAsset = imageAsset;
        this.imageRole = MenuImageRole.PRIMARY;
        this.sortOrder = 0;
        this.primary = true;
    }

    public void replaceImageAsset(ImageAsset imageAsset) {
        this.imageAsset = imageAsset;
    }
}
