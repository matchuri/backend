package matchuri.backend.domain.menu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;

@Getter
@Entity
@Table(
    name = "attribute_categories",
    comment = "공통 속성 카테고리",
    indexes = {
        @Index(name = "idx_attribute_categories_active", columnList = "is_active"),
        @Index(name = "idx_attribute_categories_type_active", columnList = "category_type,is_active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_attribute_categories_type_code", columnNames = {"category_type", "code"})
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttributeCategory extends BaseEntity {

    public static final int CODE_MAX_LENGTH = 50;
    public static final int NAME_MAX_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "속성 카테고리 ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false, length = 30, comment = "카테고리 유형")
    private CategoryType categoryType;

    @Column(nullable = false, length = CODE_MAX_LENGTH, comment = "카테고리 코드")
    private String code;

    @Column(nullable = false, length = NAME_MAX_LENGTH, comment = "카테고리명")
    private String name;

    @Column(name = "sort_order", nullable = false, comment = "정렬 순서")
    private int sortOrder;

    @Column(name = "is_active", nullable = false, comment = "활성 여부")
    private boolean active;

    public AttributeCategory(CategoryType categoryType, String code, String name, int sortOrder) {
        this.categoryType = categoryType;
        this.code = code;
        this.name = name;
        this.sortOrder = sortOrder;
        this.active = true;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
