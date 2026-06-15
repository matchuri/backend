package matchuri.backend.domain.group.entity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;
import matchuri.backend.domain.member.entity.Member;

@Getter
@Entity
@Table(
        name = "group_rooms",
        comment = "그룹 방",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_group_rooms_invite_code", columnNames = "invite_code")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupRoom extends BaseEntity {

    public static final int NAME_MAX_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "그룹 방 ID")
    private Long id;

    @Column(nullable = false, length = NAME_MAX_LENGTH, comment = "그룹명")
    private String name;

    @Column(name = "invite_code", nullable = false, length = 32, comment = "그룹 고정 초대 코드")
    private String inviteCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_member_id", nullable = false, comment = "방장 회원 ID")
    private Member hostMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "그룹 방 상태")
    private GroupRoomStatus status;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<GroupRoomMember> groupRoomMembers = new ArrayList<>();

    private GroupRoom(
            String name,
            String inviteCode,
            Member hostMember
    ) {
        this.name = name;
        this.inviteCode = inviteCode;
        this.hostMember = hostMember;
        this.status = GroupRoomStatus.ACTIVE;
    }

    public static GroupRoom createOwnedBy(
            String name,
            String inviteCode,
            Member hostMember
    ) {
        GroupRoom newGroupRoom = new GroupRoom(name, inviteCode, hostMember);
        newGroupRoom.addGroupMember(hostMember, GroupMemberRole.OWNER);
        return newGroupRoom;
    }

    public void addGroupMember(Member member, GroupMemberRole role) {
        GroupRoomMember groupRoomMember = new GroupRoomMember(this, member, role, LocalDateTime.now());
        groupRoomMembers.add(groupRoomMember);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void close() {
        this.status = GroupRoomStatus.CLOSED;
    }

    public void delete() {
        this.status = GroupRoomStatus.DELETED;
    }

    public boolean isActive() {
        return this.status == GroupRoomStatus.ACTIVE;
    }

    public GroupRoomMember getGroupRoomHostMember() {
        Long hostMemberId = this.hostMember.getId();
        return this.groupRoomMembers.stream()
                .filter(groupRoomMember -> Objects.equals(groupRoomMember.getMember().getId(), hostMemberId))
                .findFirst()
                .orElse(null);
    }

    public Optional<GroupRoomMember> getGroupRoomMemberById (long memberId) {
        return this.groupRoomMembers.stream()
                .filter(groupRoomMember -> groupRoomMember.getMember().getId() == memberId)
                .findFirst();
    }
}
