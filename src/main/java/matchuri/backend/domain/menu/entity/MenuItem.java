package matchuri.backend.domain.menu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;

@Getter
@Entity
@Table(
    name = "menu_items",
    comment = "메뉴"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "메뉴 ID")
    private Long id;

    @Column(nullable = false, length = 120, comment = "메뉴명")
    private String name;

    @Column(length = 500, comment = "메뉴 설명")
    private String description;

    public MenuItem(
        String name,
        String description
    ) {
        this.name = name;
        this.description = description;
    }
}
