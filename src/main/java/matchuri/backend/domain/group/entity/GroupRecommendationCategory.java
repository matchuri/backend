package matchuri.backend.domain.group.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@Table(
        name = "group_recommendation_categories",
        comment = "그룹 추천 당시 선택한 카테고리",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_group_recommendation_category", columnNames = {
                        "group_recommendation_id", "attribute_category_id"}),
                @UniqueConstraint(name = "uk_group_recommendation_category_rank", columnNames = {
                        "group_recommendation_id", "rank_no"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupRecommendationCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "그룹 추천 카테고리 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_recommendation_id", nullable = false, comment = "그룹 추천 ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GroupRecommendation groupRecommendation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_category_id", nullable = false, comment = "속성 카테고리 ID")
    private AttributeCategory attributeCategory;

    @Column(name = "rank_no", nullable = false, comment = "표시 순위")
    private int rankNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "카테고리 선택 출처")
    private GroupRecommendationCategorySource source;

    public GroupRecommendationCategory(
            GroupRecommendation groupRecommendation,
            AttributeCategory attributeCategory,
            int rankNo,
            GroupRecommendationCategorySource source
    ) {
        this.groupRecommendation = groupRecommendation;
        this.attributeCategory = attributeCategory;
        this.rankNo = rankNo;
        this.source = source;
    }
}
