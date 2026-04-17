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
    name = "menu_ingredients",
    comment = "메뉴-재료 매핑",
    indexes = {
        @Index(name = "idx_menu_ingredients_menu_id", columnList = "menu_id"),
        @Index(name = "idx_menu_ingredients_ingredient_id", columnList = "ingredient_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_menu_ingredient", columnNames = {"menu_id", "ingredient_id"})
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuIngredient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "메뉴-재료 매핑 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false, comment = "메뉴 ID")
    private MenuItem menu;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false, comment = "재료 ID")
    private Ingredient ingredient;

    public MenuIngredient(MenuItem menu, Ingredient ingredient) {
        this.menu = menu;
        this.ingredient = ingredient;
    }
}
