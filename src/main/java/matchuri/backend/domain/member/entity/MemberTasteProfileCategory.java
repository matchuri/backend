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
import matchuri.backend.domain.menu.entity.AttributeCategory;

@Getter
@Entity
@Table(
        name = "member_taste_profile_categories",
        comment = "회원 취향 프로필-속성 카테고리 매핑",
        indexes = {
                @Index(name = "idx_member_taste_profile_categories_profile_id", columnList = "profile_id"),
                @Index(name = "idx_member_taste_profile_categories_attribute_category_id", columnList = "attribute_category_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_taste_profile_category",
                        columnNames = {"profile_id", "attribute_category_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTasteProfileCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "회원 취향 프로필-속성 카테고리 매핑 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, comment = "회원 취향 프로필 ID")
    private MemberTasteProfile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_category_id", nullable = false, comment = "속성 카테고리 ID")
    private AttributeCategory attributeCategory;

    public MemberTasteProfileCategory(MemberTasteProfile profile, AttributeCategory attributeCategory) {
        this.profile = profile;
        this.attributeCategory = attributeCategory;
    }
}
