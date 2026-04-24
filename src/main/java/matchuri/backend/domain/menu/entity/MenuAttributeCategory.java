package matchuri.backend.domain.menu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;

@Getter
@Entity
@Table(
        name = "menu_attribute_categories",
        comment = "메뉴-속성 카테고리 매핑",
        indexes = {
                @Index(name = "idx_menu_attr_categories_menu_id", columnList = "menu_id"),
                @Index(name = "idx_menu_attr_categories_category_id", columnList = "attribute_category_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_menu_attribute_category", columnNames = {"menu_id",
                        "attribute_category_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuAttributeCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "메뉴-속성 카테고리 매핑 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false, comment = "메뉴 ID")
    private MenuItem menu;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_category_id", nullable = false, comment = "속성 카테고리 ID")
    private AttributeCategory attributeCategory;

    public MenuAttributeCategory(MenuItem menu, AttributeCategory attributeCategory) {
        this.menu = menu;
        this.attributeCategory = attributeCategory;
    }
}
