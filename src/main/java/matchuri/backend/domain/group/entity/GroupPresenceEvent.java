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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.CreatedAtEntity;
import matchuri.backend.domain.member.entity.Member;

@Getter
@Entity
@Table(
        name = "group_presence_events",
        comment = "그룹 입퇴장 이벤트"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupPresenceEvent extends CreatedAtEntity {

    public static final int WEBSOCKET_SESSION_ID_MAX_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "그룹 입퇴장 이벤트 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, comment = "그룹 방 ID")
    private GroupRoom room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, comment = "회원 ID")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20, comment = "입퇴장 이벤트 유형")
    private GroupPresenceEventType eventType;

    @Column(name = "websocket_session_id", length = WEBSOCKET_SESSION_ID_MAX_LENGTH, comment = "웹소켓 세션 ID")
    private String websocketSessionId;

    public GroupPresenceEvent(
            GroupRoom room,
            Member member,
            GroupPresenceEventType eventType,
            String websocketSessionId
    ) {
        this.room = room;
        this.member = member;
        this.eventType = eventType;
        this.websocketSessionId = websocketSessionId;
    }
}