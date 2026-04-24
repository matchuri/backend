package matchuri.backend.domain.menu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "ingredients",
        comment = "재료",
        indexes = {
                @Index(name = "idx_ingredients_active", columnList = "is_active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ingredients_code", columnNames = "code")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ingredient extends BaseEntity {

    public static final int CODE_MAX_LENGTH = 50;
    public static final int NAME_MAX_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "재료 ID")
    private Long id;

    @Column(nullable = false, length = CODE_MAX_LENGTH, comment = "재료 코드")
    private String code;

    @Column(nullable = false, length = NAME_MAX_LENGTH, comment = "재료명")
    private String name;

    @Column(name = "is_allergen", nullable = false, comment = "알레르기 유발 여부")
    private boolean allergen;

    @Column(name = "sort_order", nullable = false, comment = "정렬 순서")
    private int sortOrder;

    @Column(name = "is_active", nullable = false, comment = "활성 여부")
    private boolean active;

    public Ingredient(String code, String name, boolean allergen, int sortOrder) {
        this.code = code;
        this.name = name;
        this.allergen = allergen;
        this.sortOrder = sortOrder;
        this.active = true;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateAllergen(boolean allergen) {
        this.allergen = allergen;
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
