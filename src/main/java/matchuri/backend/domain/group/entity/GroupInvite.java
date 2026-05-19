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
import matchuri.backend.domain.member.entity.Member;

@Getter
@Entity
@Table(
        name = "group_invites",
        comment = "그룹 초대"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupInvite extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "그룹 초대 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, comment = "그룹 방 ID")
    private GroupRoom room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_member_id", nullable = false, comment = "초대 생성 회원 ID")
    private Member requestMember;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_member_id", nullable = false, comment = "초대 대상 회원 ID")
    private Member targetMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "초대 상태")
    private GroupInviteStatus status;

    @Column(name = "expires_at", comment = "만료 시각")
    private LocalDateTime expiresAt;

    @Column(name = "responded_at", comment = "응답 시각")
    private LocalDateTime respondedAt;

    public GroupInvite(GroupRoom room, Member requestMember, Member targetMember, LocalDateTime expiresAt) {
        this.room = room;
        this.requestMember = requestMember;
        this.targetMember = targetMember;
        this.expiresAt = expiresAt;
        this.status = GroupInviteStatus.PENDING;
    }

    public void accept(LocalDateTime respondedAt) {
        this.status = GroupInviteStatus.ACCEPTED;
        this.respondedAt = respondedAt;
    }

    public void decline(LocalDateTime respondedAt) {
        this.status = GroupInviteStatus.DECLINED;
        this.respondedAt = respondedAt;
    }

    public void expire() {
        this.status = GroupInviteStatus.EXPIRED;
    }

    public void revoke() {
        this.status = GroupInviteStatus.REVOKED;
    }
}
