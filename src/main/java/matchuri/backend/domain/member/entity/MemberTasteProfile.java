package matchuri.backend.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.entity.MenuItem;

@Getter
@Entity
@Table(
        name = "member_taste_profiles",
        comment = "회원 취향 프로필",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_taste_profiles_member", columnNames = "member_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTasteProfile extends BaseEntity {

    public static final int PROFILE_VERSION_MAX_SIZE = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "회원 취향 프로필 ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true, comment = "회원 ID")
    private Member member;

    @Column(name = "profile_version", nullable = false, length = PROFILE_VERSION_MAX_SIZE, comment = "프로필 버전")
    private String profileVersion;

    @OneToMany(mappedBy = "profile")
    private List<MemberTasteProfileCategory> preferAttributeCategories = new ArrayList<>();

    @OneToMany(mappedBy = "profile")
    private List<MemberTasteProfileRestrictionIngredient> restrictionIngredients = new ArrayList<>();

    @OneToMany(mappedBy = "profile")
    private List<MemberTasteProfileDislikedMenuItem> dislikedMenuItems = new ArrayList<>();

    public MemberTasteProfile(Member member, String profileVersion) {
        this.member = member;
        this.profileVersion = profileVersion;
        member.attachTasteProfile(this);
    }

    public void updateProfileVersion(String profileVersion) {
        this.profileVersion = profileVersion;
    }

    public List<Ingredient> getRestrictionIngredients() {
        return restrictionIngredients.stream()
                .map(MemberTasteProfileRestrictionIngredient::getIngredient)
                .toList();
    }

    public List<MenuItem> getDisLikeMenuItems() {
        return dislikedMenuItems.stream()
                .map(MemberTasteProfileDislikedMenuItem::getMenuItem)
                .toList();
    }

    public List<AttributeCategory> getPreferAttributeCategories() {
        return preferAttributeCategories.stream()
                .map(MemberTasteProfileCategory::getAttributeCategory)
                .toList();
    }
}
