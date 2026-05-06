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
import matchuri.backend.domain.common.CreatedAtEntity;
import matchuri.backend.domain.member.entity.Member;

@Getter
@Entity
@Table(
        name = "group_recommendation_votes",
        comment = "그룹 추천 투표",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_recommendation_vote_member",
                        columnNames = {"group_recommendation_id", "member_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupRecommendationVote extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "그룹 추천 투표 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_recommendation_id", nullable = false, comment = "그룹 추천 ID")
    private GroupRecommendation groupRecommendation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false, comment = "그룹 추천 후보 ID")
    private GroupRecommendationCandidate candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, comment = "투표 회원 ID")
    private Member member;

    public GroupRecommendationVote(
            GroupRecommendation groupRecommendation,
            GroupRecommendationCandidate candidate,
            Member member
    ) {
        this.groupRecommendation = groupRecommendation;
        this.candidate = candidate;
        this.member = member;
    }
}