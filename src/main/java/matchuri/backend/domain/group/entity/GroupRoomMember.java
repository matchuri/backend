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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;
import matchuri.backend.domain.member.entity.Member;

@Getter
@Entity
@Table(
        name = "group_room_members",
        comment = "그룹 방 멤버",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_group_room_member", columnNames = {"room_id", "member_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupRoomMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "그룹 방 멤버 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, comment = "그룹 방 ID")
    private GroupRoom room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, comment = "회원 ID")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "그룹 멤버 역할")
    private GroupMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "그룹 멤버 상태")
    private GroupMemberStatus status;

    @Column(name = "joined_at", nullable = false, comment = "참여 시각")
    private LocalDateTime joinedAt;

    @Column(name = "left_at", comment = "퇴장 시각")
    private LocalDateTime leftAt;

    public GroupRoomMember(GroupRoom room, Member member, GroupMemberRole role, LocalDateTime joinedAt) {
        this.room = room;
        this.member = member;
        this.role = role;
        this.joinedAt = joinedAt;
        this.status = GroupMemberStatus.ACTIVE;
    }

    public void leave(LocalDateTime leftAt) {
        this.status = GroupMemberStatus.LEFT;
        this.leftAt = leftAt;
    }

    public void kick(LocalDateTime leftAt) {
        this.status = GroupMemberStatus.KICKED;
        this.leftAt = leftAt;
    }

    public void rejoin(LocalDateTime joinedAt) {
        this.status = GroupMemberStatus.ACTIVE;
        this.joinedAt = joinedAt;
        this.leftAt = null;
    }

    public boolean isOwner() {
        return this.role == GroupMemberRole.OWNER;
    }
}
