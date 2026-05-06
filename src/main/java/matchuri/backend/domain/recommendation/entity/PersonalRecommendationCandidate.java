package matchuri.backend.domain.recommendation.entity;

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
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;
import matchuri.backend.domain.menu.entity.MenuItem;

@Getter
@Entity
@Table(
        name = "personal_recommendation_candidates",
        comment = "개인 추천 후보",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_personal_recommendation_candidate_menu",
                        columnNames = {"personal_recommendation_id", "menu_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalRecommendationCandidate extends BaseEntity {

    public static final int REASON_SUMMARY_MAX_LENGTH = 300;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "개인 추천 후보 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_recommendation_id", nullable = false, comment = "개인 추천 ID")
    private PersonalRecommendation personalRecommendation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false, comment = "메뉴 ID")
    private MenuItem menuItem;

    @Column(name = "rank_no", nullable = false, comment = "후보 순위")
    private int rankNo;

    @Column(precision = 10, scale = 4, comment = "추천 점수")
    private BigDecimal score;

    @Column(name = "reason_summary", length = REASON_SUMMARY_MAX_LENGTH, comment = "추천 사유 요약")
    private String reasonSummary;

    @Column(name = "candidate_meta_json", columnDefinition = "json", comment = "후보 메타 JSON")
    private String candidateMetaJson;

    public PersonalRecommendationCandidate(
            PersonalRecommendation personalRecommendation,
            MenuItem menuItem,
            int rankNo,
            BigDecimal score,
            String reasonSummary,
            String candidateMetaJson
    ) {
        this.personalRecommendation = personalRecommendation;
        this.menuItem = menuItem;
        this.rankNo = rankNo;
        this.score = score;
        this.reasonSummary = reasonSummary;
        this.candidateMetaJson = candidateMetaJson;
    }
}