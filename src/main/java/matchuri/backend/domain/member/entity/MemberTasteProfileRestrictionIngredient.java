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
import matchuri.backend.domain.menu.entity.Ingredient;

@Getter
@Entity
@Table(
        name = "member_taste_profile_restriction_ingredients",
        comment = "회원 취향 프로필 제한 재료 매핑",
        indexes = {
                @Index(name = "idx_member_profile_restrictions_profile", columnList = "profile_id"),
                @Index(name = "idx_member_profile_restrictions_ingredient", columnList = "ingredient_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_profile_restriction_ingredient",
                        columnNames = {"profile_id", "ingredient_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTasteProfileRestrictionIngredient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "회원 취향 프로필 제한 재료 매핑 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, comment = "회원 취향 프로필 ID")
    private MemberTasteProfile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false, comment = "재료 ID")
    private Ingredient ingredient;

    public MemberTasteProfileRestrictionIngredient(MemberTasteProfile profile, Ingredient ingredient) {
        this.profile = profile;
        this.ingredient = ingredient;
    }
}
