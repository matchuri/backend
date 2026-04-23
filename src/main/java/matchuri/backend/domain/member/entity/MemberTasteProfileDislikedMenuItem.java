package matchuri.backend.domain.member.entity;

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
import matchuri.backend.domain.menu.entity.MenuItem;

@Getter
@Entity
@Table(
    name = "member_taste_profile_disliked_menu_items",
    comment = "회원 취향 프로필 비선호 메뉴 매핑",
    indexes = {
        @Index(name = "idx_member_profile_disliked_menu_profile", columnList = "profile_id"),
        @Index(name = "idx_member_profile_disliked_menu_menu", columnList = "menu_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_member_profile_disliked_menu_item",
            columnNames = {"profile_id", "menu_id"}
        )
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTasteProfileDislikedMenuItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "회원 취향 프로필 비선호 메뉴 매핑 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, comment = "회원 취향 프로필 ID")
    private MemberTasteProfile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false, comment = "메뉴 ID")
    private MenuItem menuItem;

    public MemberTasteProfileDislikedMenuItem(MemberTasteProfile profile, MenuItem menuItem) {
        this.profile = profile;
        this.menuItem = menuItem;
    }
}
