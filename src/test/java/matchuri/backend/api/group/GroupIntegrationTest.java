package matchuri.backend.api.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import javax.crypto.SecretKey;
import matchuri.backend.domain.group.entity.GroupInvite;
import matchuri.backend.domain.group.entity.GroupInviteStatus;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupMemberStatus;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.repository.GroupInviteRepository;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementVersions;
import matchuri.backend.global.config.MatchuriProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MatchuriProperties matchuriProperties;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GroupRoomRepository groupRoomRepository;

    @Autowired
    private GroupRoomMemberRepository groupRoomMemberRepository;

    @Autowired
    private GroupInviteRepository groupInviteRepository;

    @BeforeEach
    void setUp() {
        clearData();
    }

    @AfterEach
    void tearDown() {
        clearData();
    }

    private void clearData() {
        groupInviteRepository.deleteAll();
        groupRoomMemberRepository.deleteAll();
        groupRoomRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("그룹 생성은 방과 OWNER 멤버를 함께 저장한다")
    void createGroupCreatesRoomAndOwnerMember() throws Exception {
        Member member = saveMember("group-owner", "그룹방장");

        mockMvc.perform(post("/api/v1/groups")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "오늘 점심 메뉴 회의",
                                  "latitude": 37.498095,
                                  "longitude": 127.027610
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.groupId").isNumber())
                .andExpect(jsonPath("$.data.inviteCode").isString())
                .andExpect(jsonPath("$.data.status").value(GroupRoomStatus.ACTIVE.name()));

        assertThat(groupRoomRepository.count()).isEqualTo(1);
        assertThat(groupRoomMemberRepository.count()).isEqualTo(1);

        var savedGroup = groupRoomRepository.findAll().getFirst();
        var savedMember = groupRoomMemberRepository.findAll().getFirst();

        assertThat(savedGroup.getName()).isEqualTo("오늘 점심 메뉴 회의");
        assertThat(savedGroup.getInviteCode()).hasSize(8);
        assertThat(savedGroup.getHostMember().getId()).isEqualTo(member.getId());
        assertThat(savedGroup.getLatitude()).isEqualByComparingTo("37.498095");
        assertThat(savedGroup.getLongitude()).isEqualByComparingTo("127.027610");
        assertThat(savedGroup.getStatus()).isEqualTo(GroupRoomStatus.ACTIVE);
        assertThat(savedMember.getRoom().getId()).isEqualTo(savedGroup.getId());
        assertThat(savedMember.getMember().getId()).isEqualTo(member.getId());
        assertThat(savedMember.getRole()).isEqualTo(GroupMemberRole.OWNER);
        assertThat(savedMember.getStatus()).isEqualTo(GroupMemberStatus.ACTIVE);
        assertThat(savedMember.getJoinedAt()).isNotNull();
    }

    @Test
    @DisplayName("그룹 생성 요청은 그룹 이름이 비어 있으면 실패한다")
    void createGroupFailsWithBlankName() throws Exception {
        Member member = saveMember("blank-group-owner", "빈이름방장");

        mockMvc.perform(post("/api/v1/groups")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " ",
                                  "latitude": 37.498095,
                                  "longitude": 127.027610
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        assertThat(groupRoomRepository.count()).isZero();
        assertThat(groupRoomMemberRepository.count()).isZero();
    }

    @Test
    @DisplayName("내 그룹 목록은 현재 회원이 활성 멤버인 삭제되지 않은 그룹만 조회한다")
    void getMyGroupsReturnsActiveMembershipRoomsOnly() throws Exception {
        Member member = saveMember("group-list-user", "목록사용자");
        Member coworker = saveMember("group-coworker", "동료");
        Member other = saveMember("other-owner", "다른방장");
        GroupRoom visibleGroup = saveGroupOwnedBy(member, "같이 먹는 점심");
        groupRoomMemberRepository.save(new GroupRoomMember(
                visibleGroup,
                coworker,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        GroupRoom deletedGroup = GroupRoom.createOwnedBy("삭제된 그룹", nextInviteCode(), member, null, null);
        deletedGroup.delete();
        groupRoomRepository.save(deletedGroup);
        GroupRoom leftGroup = saveGroupOwnedBy(member, "나간 그룹");
        leaveOwnerMembership(leftGroup, member);
        saveGroupOwnedBy(other, "다른 사람 그룹");

        mockMvc.perform(get("/api/v1/groups")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(visibleGroup.getId()))
                .andExpect(jsonPath("$.data.content[0].name").value("같이 먹는 점심"))
                .andExpect(jsonPath("$.data.content[0].status").value(GroupRoomStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.data.content[0].memberCount").value(2))
                .andExpect(jsonPath("$.data.content[0].latestRecommendationStatus").value(nullValue()))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.data.pageInfo.totalPages").value(1));
    }

    @Test
    @DisplayName("내 그룹 목록은 그룹 상태 필터와 페이지네이션을 적용한다")
    void getMyGroupsAppliesStatusFilterAndPagination() throws Exception {
        Member member = saveMember("group-filter-user", "필터사용자");
        saveGroupOwnedBy(member, "활성 그룹");
        GroupRoom closedGroup = GroupRoom.createOwnedBy("닫힌 그룹", nextInviteCode(), member, null, null);
        closedGroup.close();
        groupRoomRepository.save(closedGroup);

        mockMvc.perform(get("/api/v1/groups")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .param("status", GroupRoomStatus.CLOSED.name())
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(closedGroup.getId()))
                .andExpect(jsonPath("$.data.content[0].status").value(GroupRoomStatus.CLOSED.name()))
                .andExpect(jsonPath("$.data.pageInfo.size").value(1))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1));
    }

    @Test
    @DisplayName("그룹 상세 조회는 활성 멤버에게 그룹과 활성 멤버 목록을 반환한다")
    void getGroupReturnsDetailAndActiveMembers() throws Exception {
        Member owner = saveMember("detail-owner", "상세방장");
        Member activeMember = saveMember("detail-member", "상세멤버");
        Member leftMember = saveMember("detail-left", "나간멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "상세 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                activeMember,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        GroupRoomMember leftMembership = groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                leftMember,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        leftMembership.leave(LocalDateTime.now());
        groupRoomMemberRepository.save(leftMembership);

        mockMvc.perform(get("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.name").value("상세 그룹"))
                .andExpect(jsonPath("$.data.inviteCode").value(groupRoom.getInviteCode()))
                .andExpect(jsonPath("$.data.status").value(GroupRoomStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.data.members.length()").value(2))
                .andExpect(jsonPath("$.data.members[0].memberId").value(owner.getId()))
                .andExpect(jsonPath("$.data.members[0].nickname").value("상세방장"))
                .andExpect(jsonPath("$.data.members[0].role").value(GroupMemberRole.OWNER.name()))
                .andExpect(jsonPath("$.data.members[1].memberId").value(activeMember.getId()))
                .andExpect(jsonPath("$.data.members[1].nickname").value("상세멤버"))
                .andExpect(jsonPath("$.data.activeRecommendation").value(nullValue()));
    }

    @Test
    @DisplayName("그룹 상세 조회는 비멤버 접근을 거절한다")
    void getGroupFailsForNonMember() throws Exception {
        Member owner = saveMember("forbidden-owner", "권한방장");
        Member other = saveMember("forbidden-other", "권한없음");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "권한 그룹");

        mockMvc.perform(get("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(other))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GROUP_ACCESS_DENIED"));
    }

    @Test
    @DisplayName("그룹 상세 조회는 삭제된 그룹을 찾을 수 없음으로 처리한다")
    void getGroupFailsForDeletedGroup() throws Exception {
        Member owner = saveMember("deleted-detail-owner", "삭제상세방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "삭제 상세 그룹");
        groupRoom.delete();
        groupRoomRepository.save(groupRoom);

        mockMvc.perform(get("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("GROUP_NOT_FOUND"));
    }

    @Test
    @DisplayName("그룹 수정은 OWNER가 그룹 이름을 변경한다")
    void updateGroupChangesNameForOwner() throws Exception {
        Member owner = saveMember("update-owner", "수정방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "수정 전 그룹");

        mockMvc.perform(patch("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "수정 후 그룹"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.groupId").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.name").value("수정 후 그룹"))
                .andExpect(jsonPath("$.data.latitude").value(nullValue()))
                .andExpect(jsonPath("$.data.longitude").value(nullValue()))
                .andExpect(jsonPath("$.data.status").value(GroupRoomStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());

        assertThat(groupRoomRepository.findById(groupRoom.getId()).orElseThrow().getName())
                .isEqualTo("수정 후 그룹");
    }

    @Test
    @DisplayName("그룹 수정은 OWNER가 위치만 변경할 수 있다")
    void updateGroupChangesLocationForOwner() throws Exception {
        Member owner = saveMember("update-location-owner", "위치수정방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "위치 수정 그룹");

        mockMvc.perform(patch("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "latitude": 37.498095,
                                  "longitude": 127.027610
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupId").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.name").value("위치 수정 그룹"))
                .andExpect(jsonPath("$.data.latitude").value(37.498095))
                .andExpect(jsonPath("$.data.longitude").value(127.027610))
                .andExpect(jsonPath("$.data.status").value(GroupRoomStatus.ACTIVE.name()));

        GroupRoom updatedGroup = groupRoomRepository.findById(groupRoom.getId()).orElseThrow();
        assertThat(updatedGroup.getName()).isEqualTo("위치 수정 그룹");
        assertThat(updatedGroup.getLatitude()).isEqualByComparingTo("37.498095");
        assertThat(updatedGroup.getLongitude()).isEqualByComparingTo("127.027610");
    }

    @Test
    @DisplayName("그룹 수정은 OWNER가 아닌 활성 멤버이면 거절한다")
    void updateGroupFailsForNonOwnerMember() throws Exception {
        Member owner = saveMember("update-non-owner-host", "수정권한방장");
        Member member = saveMember("update-non-owner-member", "수정권한멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "수정 권한 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));

        mockMvc.perform(patch("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "권한 없는 수정"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GROUP_UPDATE_FORBIDDEN"));

        assertThat(groupRoomRepository.findById(groupRoom.getId()).orElseThrow().getName())
                .isEqualTo("수정 권한 그룹");
    }

    @Test
    @DisplayName("그룹 수정은 비멤버이면 거절한다")
    void updateGroupFailsForNonMember() throws Exception {
        Member owner = saveMember("update-access-owner", "수정접근방장");
        Member other = saveMember("update-access-other", "수정접근없음");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "수정 접근 그룹");

        mockMvc.perform(patch("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(other)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "비멤버 수정"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GROUP_ACCESS_DENIED"));
    }

    @Test
    @DisplayName("그룹 수정은 수정할 필드가 없으면 실패한다")
    void updateGroupFailsForEmptyRequest() throws Exception {
        Member owner = saveMember("update-empty-owner", "빈수정방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "빈 수정 그룹");

        mockMvc.perform(patch("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("GROUP_UPDATE_EMPTY_REQUEST"));
    }

    @Test
    @DisplayName("그룹 수정은 이름이 blank이면 실패한다")
    void updateGroupFailsWithBlankName() throws Exception {
        Member owner = saveMember("update-blank-owner", "공백수정방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "공백 수정 그룹");

        mockMvc.perform(patch("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("그룹 수정은 위치 범위를 벗어나면 실패한다")
    void updateGroupFailsWithInvalidLocation() throws Exception {
        Member owner = saveMember("update-invalid-location-owner", "위치범위방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "위치 범위 그룹");

        mockMvc.perform(patch("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "latitude": 91.0,
                                  "longitude": 181.0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("그룹 수정은 ACTIVE 상태가 아니면 실패한다")
    void updateGroupFailsForNotActiveGroup() throws Exception {
        Member owner = saveMember("update-closed-owner", "닫힌수정방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "닫힌 수정 그룹");
        groupRoom.close();
        groupRoomRepository.save(groupRoom);

        mockMvc.perform(patch("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "닫힌 그룹 수정"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_NOT_ACTIVE"));
    }

    @Test
    @DisplayName("그룹 수정은 삭제된 그룹이면 찾을 수 없음으로 처리한다")
    void updateGroupFailsForDeletedGroup() throws Exception {
        Member owner = saveMember("update-deleted-owner", "삭제수정방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "삭제 수정 그룹");
        groupRoom.delete();
        groupRoomRepository.save(groupRoom);

        mockMvc.perform(patch("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "삭제 그룹 수정"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("GROUP_NOT_FOUND"));
    }

    @Test
    @DisplayName("닉네임 그룹 초대 생성은 OWNER가 대상 회원에게 PENDING 초대를 저장한다")
    void createNicknameInviteCreatesPendingInviteForOwner() throws Exception {
        Member owner = saveMember("nickname-invite-owner", "닉초대방장");
        Member target = saveMember("nickname-invite-target", "닉초대대상");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "닉네임 초대 그룹");
        LocalDateTime beforeExpectedExpiry = LocalDateTime.now().plusHours(24);

        mockMvc.perform(post("/api/v1/groups/invites/nickname")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groupId": %d,
                                  "nickname": "%s"
                                }
                                """.formatted(groupRoom.getId(), target.getNickname())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.inviteId").isNumber())
                .andExpect(jsonPath("$.data.groupId").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.groupName").value("닉네임 초대 그룹"))
                .andExpect(jsonPath("$.data.targetMemberId").value(target.getId()))
                .andExpect(jsonPath("$.data.targetNickname").value(target.getNickname()))
                .andExpect(jsonPath("$.data.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value(GroupInviteStatus.PENDING.name()));

        LocalDateTime afterExpectedExpiry = LocalDateTime.now().plusHours(24);
        GroupInvite savedInvite = groupInviteRepository.findAll().getFirst();

        assertThat(savedInvite.getRoom().getId()).isEqualTo(groupRoom.getId());
        assertThat(savedInvite.getRequestMember().getId()).isEqualTo(owner.getId());
        assertThat(savedInvite.getTargetMember().getId()).isEqualTo(target.getId());
        assertThat(savedInvite.getStatus()).isEqualTo(GroupInviteStatus.PENDING);
        assertThat(savedInvite.getExpiresAt()).isBetween(beforeExpectedExpiry, afterExpectedExpiry);
        assertThat(savedInvite.getRespondedAt()).isNull();
    }

    @Test
    @DisplayName("닉네임 그룹 초대 생성은 OWNER가 아니면 거절한다")
    void createNicknameInviteFailsForNonOwner() throws Exception {
        Member owner = saveMember("nickname-non-owner-host", "닉초대방장2");
        Member requester = saveMember("nickname-non-owner-requester", "닉초대멤버");
        Member target = saveMember("nickname-non-owner-target", "닉초대대상2");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "멤버 닉네임 초대 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                requester,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/groups/invites/nickname")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(requester)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groupId": %d,
                                  "nickname": "%s"
                                }
                                """.formatted(groupRoom.getId(), target.getNickname())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GROUP_INVITE_FORBIDDEN"));

        assertThat(groupInviteRepository.count()).isZero();
    }

    @Test
    @DisplayName("닉네임 그룹 초대 생성은 대상 닉네임이 없으면 실패한다")
    void createNicknameInviteFailsForMissingTargetNickname() throws Exception {
        Member owner = saveMember("nickname-missing-owner", "닉없는대상방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "없는 대상 초대 그룹");

        mockMvc.perform(post("/api/v1/groups/invites/nickname")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groupId": %d,
                                  "nickname": "없는닉네임"
                                }
                                """.formatted(groupRoom.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("GROUP_INVITE_TARGET_NOT_FOUND"));

        assertThat(groupInviteRepository.count()).isZero();
    }

    @Test
    @DisplayName("닉네임 그룹 초대 생성은 자기 자신 초대를 거절한다")
    void createNicknameInviteFailsForSelfInvite() throws Exception {
        Member owner = saveMember("nickname-self-owner", "닉자기초대방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "자기 초대 그룹");

        mockMvc.perform(post("/api/v1/groups/invites/nickname")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groupId": %d,
                                  "nickname": "%s"
                                }
                                """.formatted(groupRoom.getId(), owner.getNickname())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_INVITE_SELF_NOT_ALLOWED"));

        assertThat(groupInviteRepository.count()).isZero();
    }

    @Test
    @DisplayName("닉네임 그룹 초대 생성은 이미 활성 멤버인 대상이면 거절한다")
    void createNicknameInviteFailsForAlreadyActiveTargetMember() throws Exception {
        Member owner = saveMember("nickname-active-owner", "닉활성방장");
        Member target = saveMember("nickname-active-target", "닉활성대상");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "활성 대상 초대 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                target,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/groups/invites/nickname")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groupId": %d,
                                  "nickname": "%s"
                                }
                                """.formatted(groupRoom.getId(), target.getNickname())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_INVITE_TARGET_ALREADY_MEMBER"));

        assertThat(groupInviteRepository.count()).isZero();
    }

    @Test
    @DisplayName("닉네임 그룹 초대 생성은 같은 그룹과 대상의 PENDING 초대가 있으면 거절한다")
    void createNicknameInviteFailsForDuplicatePendingInvite() throws Exception {
        Member owner = saveMember("nickname-duplicate-owner", "닉중복방장");
        Member target = saveMember("nickname-duplicate-target", "닉중복대상");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "중복 초대 그룹");
        saveInvite(groupRoom, owner, target, LocalDateTime.now().plusHours(1));

        mockMvc.perform(post("/api/v1/groups/invites/nickname")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groupId": %d,
                                  "nickname": "%s"
                                }
                                """.formatted(groupRoom.getId(), target.getNickname())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_INVITE_ALREADY_PENDING"));

        assertThat(groupInviteRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("초대 코드 입장은 신규 멤버를 ACTIVE 멤버로 저장한다")
    void joinGroupCreatesActiveMember() throws Exception {
        Member owner = saveMember("join-owner", "입장방장");
        Member newMember = saveMember("join-new-member", "입장멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "입장 그룹");

        mockMvc.perform(post("/api/v1/groups/join")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(newMember)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inviteCode": "%s"
                                }
                                """.formatted(groupRoom.getInviteCode())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.groupId").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.memberStatus").value(GroupMemberStatus.ACTIVE.name()));

        GroupRoomMember savedMembership = groupRoomMemberRepository
                .findByRoomIdAndMemberId(groupRoom.getId(), newMember.getId())
                .orElseThrow();

        assertThat(savedMembership.getRole()).isEqualTo(GroupMemberRole.MEMBER);
        assertThat(savedMembership.getStatus()).isEqualTo(GroupMemberStatus.ACTIVE);
        assertThat(savedMembership.getJoinedAt()).isNotNull();
        assertThat(savedMembership.getLeftAt()).isNull();
    }

    @Test
    @DisplayName("초대 코드 입장은 LEFT 멤버의 기존 membership을 재활성화한다")
    void joinGroupReactivatesLeftMember() throws Exception {
        Member owner = saveMember("rejoin-owner", "재입장방장");
        Member leftMember = saveMember("rejoin-left-member", "재입장멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "재입장 그룹");
        GroupRoomMember membership = groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                leftMember,
                GroupMemberRole.MEMBER,
                LocalDateTime.now().minusDays(2)
        ));
        membership.leave(LocalDateTime.now().minusDays(1));
        groupRoomMemberRepository.save(membership);
        LocalDateTime previousJoinedAt = membership.getJoinedAt();

        mockMvc.perform(post("/api/v1/groups/join")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(leftMember)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inviteCode": "%s"
                                }
                                """.formatted(groupRoom.getInviteCode())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupId").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.memberStatus").value(GroupMemberStatus.ACTIVE.name()));

        GroupRoomMember rejoinedMembership = groupRoomMemberRepository
                .findByRoomIdAndMemberId(groupRoom.getId(), leftMember.getId())
                .orElseThrow();

        assertThat(rejoinedMembership.getId()).isEqualTo(membership.getId());
        assertThat(rejoinedMembership.getStatus()).isEqualTo(GroupMemberStatus.ACTIVE);
        assertThat(rejoinedMembership.getLeftAt()).isNull();
        assertThat(rejoinedMembership.getJoinedAt()).isAfter(previousJoinedAt);
    }

    @Test
    @DisplayName("초대 코드 입장은 이미 활성 멤버이면 중복 참여로 실패한다")
    void joinGroupFailsForAlreadyActiveMember() throws Exception {
        Member owner = saveMember("already-join-owner", "중복방장");
        Member activeMember = saveMember("already-join-member", "중복멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "중복 입장 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                activeMember,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/groups/join")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(activeMember)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inviteCode": "%s"
                                }
                                """.formatted(groupRoom.getInviteCode())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_ALREADY_JOINED"));
    }

    @Test
    @DisplayName("초대 코드 입장은 존재하지 않는 코드이면 실패한다")
    void joinGroupFailsForMissingInvite() throws Exception {
        Member member = saveMember("missing-invite-member", "없는초대멤버");

        mockMvc.perform(post("/api/v1/groups/join")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inviteCode": "MISSING1"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("GROUP_INVITE_NOT_FOUND"));
    }

    @Test
    @DisplayName("초대 코드 입장은 연결된 그룹이 ACTIVE가 아니면 실패한다")
    void joinGroupFailsForNotActiveGroup() throws Exception {
        Member owner = saveMember("not-active-join-owner", "닫힌입장방장");
        Member member = saveMember("not-active-join-member", "닫힌입장멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "닫힌 입장 그룹");
        groupRoom.close();
        groupRoomRepository.save(groupRoom);

        mockMvc.perform(post("/api/v1/groups/join")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inviteCode": "%s"
                                }
                                """.formatted(groupRoom.getInviteCode())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_NOT_ACTIVE"));
    }

    @Test
    @DisplayName("그룹 나가기는 일반 멤버를 LEFT 상태로 전환한다")
    void leaveGroupChangesMemberStatusToLeft() throws Exception {
        Member owner = saveMember("leave-owner", "탈퇴방장");
        Member member = saveMember("leave-member", "탈퇴멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "탈퇴 그룹");
        GroupRoomMember membership = groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        LocalDateTime beforeLeave = LocalDateTime.now();

        mockMvc.perform(post("/api/v1/groups/{groupId}/leave", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.groupId").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.memberStatus").value(GroupMemberStatus.LEFT.name()))
                .andExpect(jsonPath("$.data.leftAt").isNotEmpty());

        LocalDateTime afterLeave = LocalDateTime.now();
        GroupRoomMember savedMembership = groupRoomMemberRepository.findById(membership.getId()).orElseThrow();

        assertThat(savedMembership.getStatus()).isEqualTo(GroupMemberStatus.LEFT);
        assertThat(savedMembership.getLeftAt()).isBetween(beforeLeave, afterLeave);
    }

    @Test
    @DisplayName("그룹 나가기는 OWNER이면 거절한다")
    void leaveGroupFailsForOwner() throws Exception {
        Member owner = saveMember("owner-leave-owner", "방장탈퇴");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "방장 탈퇴 그룹");

        mockMvc.perform(post("/api/v1/groups/{groupId}/leave", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_OWNER_LEAVE_NOT_ALLOWED"));

        GroupRoomMember ownerMembership = groupRoomMemberRepository
                .findByRoomIdAndMemberId(groupRoom.getId(), owner.getId())
                .orElseThrow();
        assertThat(ownerMembership.getStatus()).isEqualTo(GroupMemberStatus.ACTIVE);
        assertThat(ownerMembership.getLeftAt()).isNull();
    }

    @Test
    @DisplayName("그룹 나가기는 이미 LEFT 멤버이면 실패한다")
    void leaveGroupFailsForAlreadyLeftMember() throws Exception {
        Member owner = saveMember("already-left-owner", "이미나감방장");
        Member member = saveMember("already-left-member", "이미나감멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "이미 나간 그룹");
        GroupRoomMember membership = groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        membership.leave(LocalDateTime.now());
        groupRoomMemberRepository.save(membership);

        mockMvc.perform(post("/api/v1/groups/{groupId}/leave", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_MEMBER_ALREADY_LEFT"));
    }

    @Test
    @DisplayName("그룹 나가기는 그룹 멤버가 아니면 실패한다")
    void leaveGroupFailsForNonMember() throws Exception {
        Member owner = saveMember("leave-nonmember-owner", "비멤버방장");
        Member other = saveMember("leave-nonmember-other", "비멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "비멤버 탈퇴 그룹");

        mockMvc.perform(post("/api/v1/groups/{groupId}/leave", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(other))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("GROUP_MEMBER_NOT_FOUND"));
    }

    @Test
    @DisplayName("그룹 나가기는 삭제된 그룹이면 찾을 수 없음으로 처리한다")
    void leaveGroupFailsForDeletedGroup() throws Exception {
        Member owner = saveMember("deleted-leave-owner", "삭제탈퇴방장");
        Member member = saveMember("deleted-leave-member", "삭제탈퇴멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "삭제 탈퇴 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        groupRoom.delete();
        groupRoomRepository.save(groupRoom);

        mockMvc.perform(post("/api/v1/groups/{groupId}/leave", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("GROUP_NOT_FOUND"));
    }

    @Test
    @DisplayName("그룹 삭제는 방을 DELETED로 전환하고 활성 초대와 활성 멤버를 정리한다")
    void deleteGroupSoftDeletesRoomAndCleansActiveRelations() throws Exception {
        Member owner = saveMember("delete-owner", "삭제방장");
        Member activeMember = saveMember("delete-member", "삭제멤버");
        Member leftMember = saveMember("delete-left-member", "삭제전나간멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "삭제 그룹");
        GroupRoomMember activeMembership = groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                activeMember,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        GroupRoomMember leftMembership = groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                leftMember,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        LocalDateTime alreadyLeftAt = LocalDateTime.of(2026, 5, 18, 9, 0);
        leftMembership.leave(alreadyLeftAt);
        groupRoomMemberRepository.save(leftMembership);
        GroupInvite pendingInvite = saveInvite(groupRoom, owner, activeMember, LocalDateTime.now().plusHours(1));
        GroupInvite declinedInvite = saveInvite(groupRoom, owner, leftMember, LocalDateTime.now().plusHours(1));
        declinedInvite.decline(LocalDateTime.now());
        groupInviteRepository.save(declinedInvite);

        mockMvc.perform(delete("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.groupId").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.status").value(GroupRoomStatus.DELETED.name()))
                .andExpect(jsonPath("$.data.deletedAt").isNotEmpty());

        GroupRoom deletedGroup = groupRoomRepository.findById(groupRoom.getId()).orElseThrow();
        GroupRoomMember ownerMembership = groupRoomMemberRepository
                .findByRoomIdAndMemberId(groupRoom.getId(), owner.getId())
                .orElseThrow();
        GroupRoomMember savedActiveMembership = groupRoomMemberRepository
                .findById(activeMembership.getId())
                .orElseThrow();
        GroupRoomMember savedLeftMembership = groupRoomMemberRepository
                .findById(leftMembership.getId())
                .orElseThrow();
        GroupInvite savedPendingInvite = groupInviteRepository.findById(pendingInvite.getId()).orElseThrow();
        GroupInvite savedDeclinedInvite = groupInviteRepository.findById(declinedInvite.getId()).orElseThrow();

        assertThat(deletedGroup.getStatus()).isEqualTo(GroupRoomStatus.DELETED);
        assertThat(ownerMembership.getStatus()).isEqualTo(GroupMemberStatus.LEFT);
        assertThat(savedActiveMembership.getStatus()).isEqualTo(GroupMemberStatus.LEFT);
        assertThat(ownerMembership.getLeftAt()).isNotNull();
        assertThat(savedActiveMembership.getLeftAt()).isNotNull();
        assertThat(savedLeftMembership.getStatus()).isEqualTo(GroupMemberStatus.LEFT);
        assertThat(savedLeftMembership.getLeftAt()).isEqualTo(alreadyLeftAt);
        assertThat(savedPendingInvite.getStatus()).isEqualTo(GroupInviteStatus.REVOKED);
        assertThat(savedDeclinedInvite.getStatus()).isEqualTo(GroupInviteStatus.DECLINED);
    }

    @Test
    @DisplayName("그룹 삭제는 OWNER가 아닌 활성 멤버이면 거절한다")
    void deleteGroupFailsForNonOwnerMember() throws Exception {
        Member owner = saveMember("delete-non-owner-host", "삭제권한방장");
        Member member = saveMember("delete-non-owner-member", "삭제권한멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "삭제 권한 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));

        mockMvc.perform(delete("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GROUP_DELETE_FORBIDDEN"));

        assertThat(groupRoomRepository.findById(groupRoom.getId()).orElseThrow().getStatus())
                .isEqualTo(GroupRoomStatus.ACTIVE);
    }

    @Test
    @DisplayName("그룹 삭제는 활성 멤버가 아니면 거절한다")
    void deleteGroupFailsForNonMember() throws Exception {
        Member owner = saveMember("delete-access-owner", "삭제접근방장");
        Member other = saveMember("delete-access-other", "삭제접근없음");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "삭제 접근 그룹");

        mockMvc.perform(delete("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(other))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GROUP_ACCESS_DENIED"));
    }

    @Test
    @DisplayName("그룹 삭제는 삭제된 그룹이면 찾을 수 없음으로 처리한다")
    void deleteGroupFailsForDeletedGroup() throws Exception {
        Member owner = saveMember("already-deleted-owner", "이미삭제방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "이미 삭제 그룹");
        groupRoom.delete();
        groupRoomRepository.save(groupRoom);

        mockMvc.perform(delete("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("GROUP_NOT_FOUND"));
    }

    private Member saveMember(String loginId, String nickname) {
        return memberRepository.save(Member.builder()
                .loginId(loginId)
                .passwordHash("encoded-password")
                .email(loginId + "@example.com")
                .nickname(nickname)
                .nicknameCompleted(true)
                .social(false)
                .memberRole(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .build());
    }

    private GroupRoom saveGroupOwnedBy(Member member, String name) {
        return groupRoomRepository.save(GroupRoom.createOwnedBy(name, nextInviteCode(), member, null, null));
    }

    private String nextInviteCode() {
        return "T%07d".formatted(groupRoomRepository.count() + 1);
    }

    private GroupInvite saveInvite(
            GroupRoom groupRoom,
            Member createdByMember,
            Member targetMember,
            LocalDateTime expiresAt
    ) {
        return groupInviteRepository.save(new GroupInvite(groupRoom, createdByMember, targetMember, expiresAt));
    }

    private void leaveOwnerMembership(GroupRoom groupRoom, Member member) {
        GroupRoomMember membership = groupRoomMemberRepository.findAll().stream()
                .filter(candidate -> candidate.getRoom().getId().equals(groupRoom.getId()))
                .filter(candidate -> candidate.getMember().getId().equals(member.getId()))
                .findFirst()
                .orElseThrow();
        membership.leave(LocalDateTime.now());
        groupRoomMemberRepository.save(membership);
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private String accessToken(Member member) {
        Instant now = Instant.now();
        SecretKey signingKey = Keys.hmacShaKeyFor(
                matchuriProperties.getAuth().getJwt().getSecret().getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
                .issuer(matchuriProperties.getAuth().getJwt().getIssuer())
                .subject(String.valueOf(member.getId()))
                .claim("role", member.getMemberRole().name())
                .claim("loginId", member.getLoginId())
                .claim("requiredAgreementRevision", RequiredAgreementVersions.currentRevision())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(signingKey)
                .compact();
    }
}
