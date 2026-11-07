package matchuri.backend.domain.group.support.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.exception.GroupErrorCode;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroupRoomReaderTest {

    private static final long GROUP_ID = 10L;
    private static final long MEMBER_ID = 20L;

    private final GroupRoomRepository groupRoomRepository = mock(GroupRoomRepository.class);
    private final GroupRoomMemberRepository groupRoomMemberRepository = mock(GroupRoomMemberRepository.class);
    private final GroupRoomReader reader = new GroupRoomReader(groupRoomRepository, groupRoomMemberRepository);

    @Test
    @DisplayName("활성 그룹 방을 조회한다")
    void getActiveGroupRoomReturnsActiveRoom() {
        GroupRoom room = mock(GroupRoom.class);
        when(room.isActive()).thenReturn(true);
        when(groupRoomRepository.findByIdAndStatusNot(GROUP_ID, GroupRoomStatus.DELETED))
                .thenReturn(Optional.of(room));

        assertThat(reader.getActiveGroupRoom(GROUP_ID)).isSameAs(room);
    }

    @Test
    @DisplayName("닫힌 그룹 방은 활성 방으로 조회할 수 없다")
    void getActiveGroupRoomRejectsClosedRoom() {
        GroupRoom room = mock(GroupRoom.class);
        when(room.getId()).thenReturn(GROUP_ID);
        when(room.isActive()).thenReturn(false);
        when(groupRoomRepository.findByIdAndStatusNot(GROUP_ID, GroupRoomStatus.DELETED))
                .thenReturn(Optional.of(room));

        assertThatThrownBy(() -> reader.getActiveGroupRoom(GROUP_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.NOT_ACTIVE);
    }

    @Test
    @DisplayName("활성 멤버십이 없으면 그룹 접근을 거절한다")
    void getActiveMembershipRejectsMissingMembership() {
        when(groupRoomMemberRepository.findActiveMembershipInNotDeletedRoom(GROUP_ID, MEMBER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reader.getActiveMembership(GROUP_ID, MEMBER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("활성 멤버십을 반환한다")
    void getActiveMembershipReturnsMembership() {
        GroupRoomMember membership = mock(GroupRoomMember.class);
        when(groupRoomMemberRepository.findActiveMembershipInNotDeletedRoom(GROUP_ID, MEMBER_ID))
                .thenReturn(Optional.of(membership));

        assertThat(reader.getActiveMembership(GROUP_ID, MEMBER_ID)).isSameAs(membership);
    }
}
