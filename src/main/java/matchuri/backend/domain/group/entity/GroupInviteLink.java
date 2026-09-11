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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@Table(
        name = "group_invite_links",
        comment = "그룹 링크 초대",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_group_invite_links_token", columnNames = "token")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupInviteLink extends BaseEntity {

    public static final int TOKEN_LENGTH = 36;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "그룹 초대 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, comment = "그룹 방 ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GroupRoom room;

    @Column(name = "token", nullable = false, length = TOKEN_LENGTH, comment = "URL 끝에 붙는 UUID 기반 난수")
    private String token;

    @Column(name = "expires_at", nullable = false, comment = "만료 시각")
    private LocalDateTime expiresAt;

    public GroupInviteLink(GroupRoom room, String token, LocalDateTime expiresAt) {
        this.room = room;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public void expire(LocalDateTime expiredAt) {
        if (expiresAt.isAfter(expiredAt)) {
            expiresAt = expiredAt;
        }
    }

    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }
}
