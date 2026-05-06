package matchuri.backend.domain.behavior.entity;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.CreatedAtEntity;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;

@Getter
@Entity
@Table(
        name = "member_menu_actions",
        comment = "회원 메뉴 행동 로그"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberMenuAction extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "회원 메뉴 행동 로그 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, comment = "회원 ID")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false, comment = "메뉴 ID")
    private MenuItem menuItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_recommendation_id", comment = "개인 추천 ID")
    private PersonalRecommendation personalRecommendation;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20, comment = "행동 유형")
    private ActionType actionType;

    public MemberMenuAction(
            Member member,
            MenuItem menuItem,
            PersonalRecommendation personalRecommendation,
            ActionType actionType
    ) {
        this.member = member;
        this.menuItem = menuItem;
        this.personalRecommendation = personalRecommendation;
        this.actionType = actionType;
    }
}