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
        name = "group_invites",
        comment = "그룹 초대",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_group_invites_invite_code", columnNames = "invite_code")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupInvite extends BaseEntity {

    public static final int INVITE_CODE_MAX_LENGTH = 64;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "그룹 초대 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, comment = "그룹 방 ID")
    private GroupRoom room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_member_id", nullable = false, comment = "초대 생성 회원 ID")
    private Member createdByMember;

    @Column(name = "invite_code", nullable = false, length = INVITE_CODE_MAX_LENGTH, comment = "초대 코드")
    private String inviteCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "초대 상태")
    private GroupInviteStatus status;

    @Column(name = "expires_at", comment = "만료 시각")
    private LocalDateTime expiresAt;

    public GroupInvite(GroupRoom room, Member createdByMember, String inviteCode, LocalDateTime expiresAt) {
        this.room = room;
        this.createdByMember = createdByMember;
        this.inviteCode = inviteCode;
        this.expiresAt = expiresAt;
        this.status = GroupInviteStatus.ACTIVE;
    }

    public void expire() {
        this.status = GroupInviteStatus.EXPIRED;
    }

    public void revoke() {
        this.status = GroupInviteStatus.REVOKED;
    }
}