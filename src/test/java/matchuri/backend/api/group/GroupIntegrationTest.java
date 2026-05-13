package matchuri.backend.api.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupMemberStatus;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
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

    @BeforeEach
    void setUp() {
        clearData();
    }

    @AfterEach
    void tearDown() {
        clearData();
    }

    private void clearData() {
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
