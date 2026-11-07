package matchuri.backend.domain.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import matchuri.backend.domain.group.entity.GroupInviteLink;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.repository.GroupInviteLinkRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.global.config.JpaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest(showSql = false)
@Import(JpaConfig.class)
@ActiveProfiles("test")
class GroupInviteLinkRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GroupRoomRepository groupRoomRepository;

    @Autowired
    private GroupInviteLinkRepository groupInviteLinkRepository;

    private GroupRoom room;

    @BeforeEach
    void setUp() {
        Member owner = memberRepository.save(
                Member.createWithEncodedPassword("link-owner", "encoded-password", "링크방장", "owner@example.com")
        );
        room = groupRoomRepository.saveAndFlush(GroupRoom.createOwnedBy("링크 그룹", "INVITE-CODE", owner));
    }

    @Test
    @DisplayName("초대 링크 token은 DB unique 제약으로 중복 저장을 막는다")
    void tokenMustBeUnique() {
        String token = "550e8400-e29b-41d4-a716-446655440000";
        groupInviteLinkRepository.saveAndFlush(new GroupInviteLink(room, token, LocalDateTime.now().plusDays(1)));

        assertThatThrownBy(() -> groupInviteLinkRepository.saveAndFlush(
                new GroupInviteLink(room, token, LocalDateTime.now().plusDays(1))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("초대 링크 발급용 그룹 조회는 비관적 쓰기 잠금 쿼리를 실행한다")
    void findsActiveRoomForUpdate() {
        assertThat(groupRoomRepository.findByIdAndStatusNotForUpdate(room.getId(), GroupRoomStatus.DELETED))
                .contains(room);
    }
}
