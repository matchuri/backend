package matchuri.backend.domain.recommendation.entity;

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
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.MenuAttributeCategory;
import matchuri.backend.domain.menu.entity.MenuItem;

@Getter
@Entity
@Table(
        name = "personal_recommendations",
        comment = "개인 추천"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalRecommendation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "개인 추천 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, comment = "회원 ID")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "개인 추천 상태")
    private PersonalRecommendationStatus status;

    @Column(name = "closed_at", comment = "추천 종료 시각")
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "close_reason", length = 30, comment = "추천 종료 사유")
    private PersonalRecommendationCloseReason closeReason;

    @Column(name = "requested_at", nullable = false, comment = "추천 실행 시각")
    private LocalDateTime requestedAt;

    @Column(name = "context_json", columnDefinition = "json", comment = "개인 추천 컨텍스트 JSON")
    private String contextJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_candidate_id", comment = "최종 선택 후보 ID")
    private PersonalRecommendationCandidate selectedCandidate;

    private PersonalRecommendation(Member member, String contextJson, LocalDateTime requestedAt,
                                   PersonalRecommendationStatus status) {
        this.member = member;
        this.contextJson = contextJson;
        this.requestedAt = requestedAt;
        this.status = status;
    }

    public static PersonalRecommendation of(Member member, String contextJson) {
        return new PersonalRecommendation(
                member,
                contextJson,
                LocalDateTime.now(),
                PersonalRecommendationStatus.REQUESTED
        );
    }

    public void markFiltered() {
        this.status = PersonalRecommendationStatus.FILTERED;
    }

    public void markScored() {
        this.status = PersonalRecommendationStatus.SCORED;
    }

    public void complete() {
        this.status = PersonalRecommendationStatus.COMPLETED;
    }

    public void fail() {
        this.status = PersonalRecommendationStatus.FAILED;
    }

    public void select(PersonalRecommendationCandidate selectedCandidate, LocalDateTime closedAt) {
        this.selectedCandidate = selectedCandidate;
        close(PersonalRecommendationCloseReason.SELECTED, closedAt);
    }

    public void closeAsRerolledWithSkip(LocalDateTime closedAt) {
        close(PersonalRecommendationCloseReason.REROLLED_WITH_SKIP, closedAt);
    }

    public void closeAsRerolledWithoutSkip(LocalDateTime closedAt) {
        close(PersonalRecommendationCloseReason.REROLLED_WITHOUT_SKIP, closedAt);
    }

    public void expire(LocalDateTime closedAt) {
        close(PersonalRecommendationCloseReason.EXPIRED, closedAt);
    }

    public boolean isClosed() {
        return this.closedAt != null;
    }

    private void close(PersonalRecommendationCloseReason closeReason, LocalDateTime closedAt) {
        this.closeReason = closeReason;
        this.closedAt = closedAt;
    }

    public MenuItem getSelectedMenu() {
        return this.selectedCandidate.getMenuItem();
    }

    public List<AttributeCategory> getSelectedMenuAttributeCategory() {

        MenuItem menuItem = this.selectedCandidate.getMenuItem();
        List<MenuAttributeCategory> menuAttributeCategories = menuItem.getMenuAttributeCategories();

        return menuAttributeCategories.stream()
                .map(MenuAttributeCategory::getAttributeCategory)
                .toList();
    }
}
