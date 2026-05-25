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
import matchuri.backend.domain.common.CreatedAtEntity;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.menu.entity.MenuItem;

@Getter
@Entity
@Table(
        name = "group_menu_actions",
        comment = "그룹 메뉴 행동 로그",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_menu_action_recommendation_menu_type",
                        columnNames = {"group_recommendation_id", "menu_id", "action_type"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMenuAction extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "그룹 메뉴 행동 로그 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_room_id", nullable = false, comment = "그룹 방 ID")
    private GroupRoom groupRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_recommendation_id", nullable = false, comment = "그룹 추천 ID")
    private GroupRecommendation groupRecommendation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_member_id", nullable = false, comment = "행동 발생 회원 ID")
    private Member actorMember;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false, comment = "메뉴 ID")
    private MenuItem menuItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20, comment = "행동 유형")
    private GroupMenuActionType actionType;

    public GroupMenuAction(
            GroupRoom groupRoom,
            GroupRecommendation groupRecommendation,
            Member actorMember,
            MenuItem menuItem,
            GroupMenuActionType actionType
    ) {
        this.groupRoom = groupRoom;
        this.groupRecommendation = groupRecommendation;
        this.actorMember = actorMember;
        this.menuItem = menuItem;
        this.actionType = actionType;
    }
}
