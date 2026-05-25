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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;

@Getter
@Entity
@Table(
        name = "group_recommendations",
        comment = "그룹 추천"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupRecommendation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "그룹 추천 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, comment = "그룹 방 ID")
    private GroupRoom room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, comment = "그룹 추천 상태")
    private GroupRecommendationStatus status;

    @Column(name = "started_at", nullable = false, comment = "시작 시각")
    private LocalDateTime startedAt;

    @Column(name = "ended_at", comment = "종료 시각")
    private LocalDateTime endedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_candidate_id", comment = "최종 선택 후보 ID")
    private GroupRecommendationCandidate selectedCandidate;

    @Column(name = "context_json", columnDefinition = "json", comment = "그룹 추천 컨텍스트 JSON")
    private String contextJson;

    public GroupRecommendation(GroupRoom room, String contextJson, LocalDateTime startedAt) {
        this.room = room;
        this.contextJson = contextJson;
        this.startedAt = startedAt;
        this.status = GroupRecommendationStatus.OPEN;
    }

    public static GroupRecommendation preparing(GroupRoom room, String contextJson, LocalDateTime startedAt) {
        GroupRecommendation recommendation = new GroupRecommendation(room, contextJson, startedAt);
        recommendation.status = GroupRecommendationStatus.PREPARING;
        return recommendation;
    }

    public void open() {
        this.status = GroupRecommendationStatus.OPEN;
    }

    public void finalizeWith(GroupRecommendationCandidate selectedCandidate, LocalDateTime endedAt) {
        this.status = GroupRecommendationStatus.FINALIZED;
        this.selectedCandidate = selectedCandidate;
        this.endedAt = endedAt;
    }

    public void rerollWithSkip(LocalDateTime endedAt) {
        this.status = GroupRecommendationStatus.REROLLED_WITH_SKIP;
        this.endedAt = endedAt;
    }

    public void rerollWithoutSkip(LocalDateTime endedAt) {
        this.status = GroupRecommendationStatus.REROLLED_WITHOUT_SKIP;
        this.endedAt = endedAt;
    }

    public void cancel(LocalDateTime endedAt) {
        this.status = GroupRecommendationStatus.CANCELED;
        this.endedAt = endedAt;
    }

    public void expire(LocalDateTime endedAt) {
        this.status = GroupRecommendationStatus.EXPIRED;
        this.endedAt = endedAt;
    }

    public void fail(LocalDateTime endedAt) {
        this.status = GroupRecommendationStatus.FAILED;
        this.endedAt = endedAt;
    }
}
