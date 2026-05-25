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
import matchuri.backend.domain.member.entity.Member;

@Getter
@Entity
@Table(
        name = "group_recommendation_readiness",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_recommendation_readiness_recommendation_member",
                        columnNames = {"group_recommendation_id", "member_id"}
                )
        },
        comment = "그룹 추천 준비 상태"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupRecommendationReadiness extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "그룹 추천 준비 상태 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_recommendation_id", nullable = false, comment = "그룹 추천 ID")
    private GroupRecommendation groupRecommendation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, comment = "회원 ID")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "준비 상태")
    private GroupRecommendationReadinessStatus status;

    public GroupRecommendationReadiness(
            GroupRecommendation groupRecommendation,
            Member member
    ) {
        this.groupRecommendation = groupRecommendation;
        this.member = member;
        this.status = GroupRecommendationReadinessStatus.READY;
    }

    public void ready() {
        this.status = GroupRecommendationReadinessStatus.READY;
    }

    public void cancel() {
        this.status = GroupRecommendationReadinessStatus.CANCELED;
    }
}
