package matchuri.backend.api.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .andExpect(jsonPath("$.data.status").value(GroupRoomStatus.ACTIVE.name()));

        assertThat(groupRoomRepository.count()).isEqualTo(1);
        assertThat(groupRoomMemberRepository.count()).isEqualTo(1);

        var savedGroup = groupRoomRepository.findAll().getFirst();
        var savedMember = groupRoomMemberRepository.findAll().getFirst();

        assertThat(savedGroup.getName()).isEqualTo("오늘 점심 메뉴 회의");
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
        GroupRoom deletedGroup = GroupRoom.createOwnedBy("삭제된 그룹", member, null, null);
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
        GroupRoom closedGroup = GroupRoom.createOwnedBy("닫힌 그룹", member, null, null);
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
    @DisplayName("그룹 초대 코드 생성은 OWNER가 활성 초대를 저장하고 반환한다")
    void createInviteCreatesActiveInviteForOwner() throws Exception {
        Member owner = saveMember("invite-owner", "초대방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "초대 그룹");
        LocalDateTime beforeExpectedExpiry = LocalDateTime.now().plusHours(24);

        mockMvc.perform(post("/api/v1/groups/{groupId}/invites", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.groupId").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.inviteCode").isString())
                .andExpect(jsonPath("$.data.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        LocalDateTime afterExpectedExpiry = LocalDateTime.now().plusHours(24);
        var savedInvite = groupInviteRepository.findAll().getFirst();

        assertThat(savedInvite.getRoom().getId()).isEqualTo(groupRoom.getId());
        assertThat(savedInvite.getCreatedByMember().getId()).isEqualTo(owner.getId());
        assertThat(savedInvite.getInviteCode()).hasSize(8);
        assertThat(savedInvite.getExpiresAt()).isBetween(beforeExpectedExpiry, afterExpectedExpiry);
        assertThat(savedInvite.getStatus().name()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("그룹 초대 코드 생성은 OWNER가 아니면 거절한다")
    void createInviteFailsForNonOwnerMember() throws Exception {
        Member owner = saveMember("invite-non-owner-host", "초대방장2");
        Member member = saveMember("invite-non-owner-member", "초대멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "멤버 초대 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/groups/{groupId}/invites", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GROUP_ACCESS_DENIED"));

        assertThat(groupInviteRepository.count()).isZero();
    }

    @Test
    @DisplayName("그룹 초대 코드 생성은 활성 상태가 아닌 그룹이면 실패한다")
    void createInviteFailsForNotActiveGroup() throws Exception {
        Member owner = saveMember("closed-invite-owner", "닫힌초대방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "닫힌 초대 그룹");
        groupRoom.close();
        groupRoomRepository.save(groupRoom);

        mockMvc.perform(post("/api/v1/groups/{groupId}/invites", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_NOT_ACTIVE"));

        assertThat(groupInviteRepository.count()).isZero();
    }

    @Test
    @DisplayName("그룹 초대 코드 생성은 삭제된 그룹이면 찾을 수 없음으로 처리한다")
    void createInviteFailsForDeletedGroup() throws Exception {
        Member owner = saveMember("deleted-invite-owner", "삭제초대방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "삭제 초대 그룹");
        groupRoom.delete();
        groupRoomRepository.save(groupRoom);

        mockMvc.perform(post("/api/v1/groups/{groupId}/invites", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("GROUP_NOT_FOUND"));

        assertThat(groupInviteRepository.count()).isZero();
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
        return groupRoomRepository.save(GroupRoom.createOwnedBy(name, member, null, null));
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
