package matchuri.backend.domain.group.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import matchuri.backend.domain.menu.entity.MenuItem;

@Getter
@Entity
@Table(
        name = "group_recommendation_candidates",
        comment = "그룹 추천 후보",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_recommendation_candidate_menu",
                        columnNames = {"group_recommendation_id", "menu_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupRecommendationCandidate extends BaseEntity {

    public static final int REASON_SUMMARY_MAX_LENGTH = 300;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "그룹 추천 후보 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_recommendation_id", nullable = false, comment = "그룹 추천 ID")
    private GroupRecommendation groupRecommendation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false, comment = "메뉴 ID")
    private MenuItem menuItem;

    @Column(name = "rank_no", nullable = false, comment = "후보 순위")
    private int rankNo;

    @Column(name = "reason_summary", length = REASON_SUMMARY_MAX_LENGTH, comment = "추천 사유 요약")
    private String reasonSummary;

    public GroupRecommendationCandidate(
            GroupRecommendation groupRecommendation,
            MenuItem menuItem,
            int rankNo,
            String reasonSummary
    ) {
        this.groupRecommendation = groupRecommendation;
        this.menuItem = menuItem;
        this.rankNo = rankNo;
        this.reasonSummary = reasonSummary;
    }
}