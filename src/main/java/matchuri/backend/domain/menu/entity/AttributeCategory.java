package matchuri.backend.domain.menu.entity;

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
    name = "attribute_categories",
    comment = "공통 속성 카테고리",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_attribute_categories_type_code", columnNames = {"category_type", "code"})
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttributeCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "속성 카테고리 ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false, length = 30, comment = "카테고리 유형")
    private CategoryType categoryType;

    @Column(nullable = false, length = 50, comment = "카테고리 코드")
    private String code;

    @Column(nullable = false, length = 100, comment = "카테고리명")
    private String name;

    public AttributeCategory(CategoryType categoryType, String code, String name) {
        this.categoryType = categoryType;
        this.code = code;
        this.name = name;
    }
}
