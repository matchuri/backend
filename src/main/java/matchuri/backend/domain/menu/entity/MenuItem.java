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
    name = "menu_items",
    comment = "메뉴",
    indexes = {
        @Index(name = "idx_menu_items_active", columnList = "is_active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_menu_items_code", columnNames = "code")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "메뉴 ID")
    private Long id;

    @Column(nullable = false, length = 50, comment = "메뉴 코드")
    private String code;

    @Column(nullable = false, length = 120, comment = "메뉴명")
    private String name;

    @Column(length = 500, comment = "메뉴 설명")
    private String description;

    @Column(name = "is_active", nullable = false, comment = "활성 여부")
    private boolean active;

    public MenuItem(
        String code,
        String name,
        String description
    ) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
