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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationCandidate;
import matchuri.backend.domain.group.entity.GroupRecommendationReadiness;
import matchuri.backend.domain.group.entity.GroupRecommendationReadinessStatus;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import matchuri.backend.domain.group.entity.GroupRecommendationVote;
import matchuri.backend.domain.group.entity.GroupLocation;
import matchuri.backend.domain.group.entity.GroupInvite;
import matchuri.backend.domain.group.entity.GroupInviteStatus;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupMemberStatus;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.group.entity.GroupRoomStatus;
import matchuri.backend.domain.group.repository.GroupInviteRepository;
import matchuri.backend.domain.group.repository.GroupLocationRepository;
import matchuri.backend.domain.group.repository.GroupMenuActionRepository;
import matchuri.backend.domain.group.repository.GroupRecommendationCandidateRepository;
import matchuri.backend.domain.group.repository.GroupRecommendationReadinessRepository;
import matchuri.backend.domain.group.repository.GroupRecommendationRepository;
import matchuri.backend.domain.group.repository.GroupRecommendationVoteRepository;
import matchuri.backend.domain.group.repository.GroupRoomMemberRepository;
import matchuri.backend.domain.group.repository.GroupRoomRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.entity.MemberTasteProfileCategory;
import matchuri.backend.domain.member.entity.MemberTasteProfileDislikedMenuItem;
import matchuri.backend.domain.member.entity.MemberTasteProfileRestrictionIngredient;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileCategoryRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileDislikedMenuItemRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRestrictionIngredientRepository;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementVersions;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.entity.MenuAttributeCategory;
import matchuri.backend.domain.menu.entity.MenuIngredient;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.repository.MenuAttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.MenuIngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;
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
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GroupRoomRepository groupRoomRepository;

    @Autowired
    private GroupLocationRepository groupLocationRepository;

    @Autowired
    private GroupRoomMemberRepository groupRoomMemberRepository;

    @Autowired
    private GroupInviteRepository groupInviteRepository;

    @Autowired
    private GroupMenuActionRepository groupMenuActionRepository;

    @Autowired
    private GroupRecommendationRepository groupRecommendationRepository;

    @Autowired
    private GroupRecommendationCandidateRepository groupRecommendationCandidateRepository;

    @Autowired
    private GroupRecommendationReadinessRepository groupRecommendationReadinessRepository;

    @Autowired
    private GroupRecommendationVoteRepository groupRecommendationVoteRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private AttributeCategoryRepository attributeCategoryRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private MenuAttributeCategoryRepository menuAttributeCategoryRepository;

    @Autowired
    private MenuIngredientRepository menuIngredientRepository;

    @Autowired
    private MemberTasteProfileRepository memberTasteProfileRepository;

    @Autowired
    private MemberTasteProfileCategoryRepository memberTasteProfileCategoryRepository;

    @Autowired
    private MemberTasteProfileRestrictionIngredientRepository memberTasteProfileRestrictionIngredientRepository;

    @Autowired
    private MemberTasteProfileDislikedMenuItemRepository memberTasteProfileDislikedMenuItemRepository;

    @BeforeEach
    void setUp() {
        clearData();
    }

    @AfterEach
    void tearDown() {
        clearData();
    }

    private void clearData() {
        jdbcTemplate.update("update group_recommendations set selected_candidate_id = null");
        groupMenuActionRepository.deleteAll();
        groupRecommendationVoteRepository.deleteAll();
        groupRecommendationReadinessRepository.deleteAll();
        groupRecommendationCandidateRepository.deleteAll();
        groupRecommendationRepository.deleteAll();
        groupInviteRepository.deleteAll();
        groupLocationRepository.deleteAll();
        groupRoomMemberRepository.deleteAll();
        groupRoomRepository.deleteAll();
        memberTasteProfileDislikedMenuItemRepository.deleteAll();
        memberTasteProfileRestrictionIngredientRepository.deleteAll();
        memberTasteProfileCategoryRepository.deleteAll();
        memberTasteProfileRepository.deleteAll();
        menuIngredientRepository.deleteAll();
        menuAttributeCategoryRepository.deleteAll();
        menuItemRepository.deleteAll();
        ingredientRepository.deleteAll();
        attributeCategoryRepository.deleteAll();
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
                                  "longitude": 127.027610,
                                  "radiusMeters": 1000,
                                  "address": "서울 강남구 테헤란로 123"
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
        GroupLocation savedLocation = latestGroupLocation(savedGroup);
        assertThat(savedLocation.getLatitude()).isEqualByComparingTo("37.498095");
        assertThat(savedLocation.getLongitude()).isEqualByComparingTo("127.027610");
        assertThat(savedLocation.getRadiusMeters()).isEqualTo(1000);
        assertThat(savedLocation.getAddress()).isEqualTo("서울 강남구 테헤란로 123");
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
    @DisplayName("그룹 추천 생성은 OWNER가 준비 세션을 저장하고 후보는 아직 생성하지 않는다")
    void createGroupRecommendationCreatesPreparingSessionForOwner() throws Exception {
        Member owner = saveMember("recommendation-owner", "추천방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "추천 그룹");

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").isNumber())
                .andExpect(jsonPath("$.data.status").value(GroupRecommendationStatus.PREPARING.name()))
                .andExpect(jsonPath("$.data.candidates.length()").value(0));

        assertThat(groupRecommendationRepository.count()).isEqualTo(1);
        assertThat(groupRecommendationCandidateRepository.count()).isZero();
        assertThat(groupRecommendationReadinessRepository.count()).isZero();

        GroupRecommendation savedRecommendation = groupRecommendationRepository.findAll().getFirst();

        assertThat(savedRecommendation.getRoom().getId()).isEqualTo(groupRoom.getId());
        assertThat(savedRecommendation.getStatus()).isEqualTo(GroupRecommendationStatus.PREPARING);
        assertThat(savedRecommendation.getContextJson()).isNull();
    }

    @Test
    @DisplayName("그룹 추천 생성은 요청 위치를 그룹의 기억 위치로 갱신한다")
    void createGroupRecommendationUpdatesRememberedGroupLocation() throws Exception {
        Member owner = saveMember("recommendation-location-owner", "추천위치방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "추천 위치 그룹");

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "latitude": 37.498095,
                                  "longitude": 127.027610,
                                  "radiusMeters": 1000,
                                  "address": "서울 강남구 테헤란로 123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(GroupRecommendationStatus.PREPARING.name()));

        GroupLocation updatedLocation = latestGroupLocation(groupRoom);
        assertThat(updatedLocation.getLatitude()).isEqualByComparingTo("37.498095");
        assertThat(updatedLocation.getLongitude()).isEqualByComparingTo("127.027610");
        assertThat(updatedLocation.getRadiusMeters()).isEqualTo(1000);
        assertThat(updatedLocation.getAddress()).isEqualTo("서울 강남구 테헤란로 123");

        GroupRecommendation savedRecommendation = groupRecommendationRepository.findAll().getFirst();
        assertThat(savedRecommendation.getContextJson()).isNull();
    }

    @Test
    @DisplayName("그룹 추천 생성은 OWNER가 아닌 활성 멤버이면 거절한다")
    void createGroupRecommendationFailsForNonOwnerMember() throws Exception {
        Member owner = saveMember("recommendation-forbidden-owner", "추천권한방장");
        Member member = saveMember("recommendation-forbidden-member", "추천권한멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "추천 권한 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_CREATE_FORBIDDEN"));

        assertThat(groupRecommendationRepository.count()).isZero();
    }

    @Test
    @DisplayName("그룹 추천 생성은 진행 중인 그룹 추천이 있으면 거절한다")
    void createGroupRecommendationFailsWhenActiveRecommendationExists() throws Exception {
        Member owner = saveMember("recommendation-open-owner", "열린추천방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "열린 추천 그룹");
        groupRecommendationRepository.save(GroupRecommendation.preparing(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_ACTIVE_EXISTS"));
    }

    @Test
    @DisplayName("그룹 추천 생성은 열린 그룹 추천이 있어도 진행 중으로 보고 거절한다")
    void createGroupRecommendationFailsWhenOpenRecommendationExists() throws Exception {
        Member owner = saveMember("recommendation-already-open-owner", "이미열린추천방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "이미 열린 추천 그룹");
        groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_ACTIVE_EXISTS"));
    }

    @Test
    @DisplayName("그룹 추천 시작은 시간상 만료된 진행 중 추천을 무시하고 새 준비 세션을 생성한다")
    void createGroupRecommendationIgnoresExpiredActiveRecommendationAndCreatesPreparingSession() throws Exception {
        Member owner = saveMember("expired-active-group-owner", "만료추천방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "만료 추천 그룹");
        GroupRecommendation oldRecommendation = groupRecommendationRepository.save(GroupRecommendation.preparing(
                groupRoom,
                "{}",
                LocalDateTime.now().minusHours(25)
        ));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(org.hamcrest.Matchers.not(oldRecommendation.getId().intValue())))
                .andExpect(jsonPath("$.data.status").value(GroupRecommendationStatus.PREPARING.name()));

        GroupRecommendation existingRecommendation = groupRecommendationRepository.findById(oldRecommendation.getId())
                .orElseThrow();
        assertThat(existingRecommendation.getStatus()).isEqualTo(GroupRecommendationStatus.PREPARING);
        assertThat(existingRecommendation.getEndedAt()).isNull();
        assertThat(groupRecommendationRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("그룹 추천 목록은 시간상 만료된 PREPARING/OPEN 추천을 제외한다")
    void getGroupRecommendationsExcludesExpiredActiveRecommendations() throws Exception {
        Member owner = saveMember("group-expiration-service-owner", "그룹만료방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "그룹 만료 서비스");
        GroupRecommendation oldPreparing = groupRecommendationRepository.save(GroupRecommendation.preparing(
                groupRoom,
                "{}",
                LocalDateTime.now().minusHours(25)
        ));
        GroupRecommendation oldOpen = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now().minusHours(25)
        ));
        GroupRecommendation recentPreparing = groupRecommendationRepository.save(GroupRecommendation.preparing(
                groupRoom,
                "{}",
                LocalDateTime.now().minusHours(23)
        ));
        GroupRecommendation alreadyClosed = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now().minusHours(25)
        ));
        alreadyClosed.rerollWithoutSkip(LocalDateTime.now().minusHours(1));
        groupRecommendationRepository.save(alreadyClosed);

        mockMvc.perform(get("/api/v1/groups/{groupId}/recommendations", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].sessionId").value(recentPreparing.getId()))
                .andExpect(jsonPath("$.data.content[1].sessionId").value(alreadyClosed.getId()));

        assertThat(groupRecommendationRepository.findById(oldPreparing.getId()).orElseThrow().getStatus())
                .isEqualTo(GroupRecommendationStatus.PREPARING);
        assertThat(groupRecommendationRepository.findById(oldOpen.getId()).orElseThrow().getStatus())
                .isEqualTo(GroupRecommendationStatus.OPEN);
    }

    @Test
    @DisplayName("그룹 추천 생성은 준비 세션만 만들기 때문에 제한 재료 후보 필터링을 아직 수행하지 않는다")
    void createGroupRecommendationDoesNotGenerateCandidatesDuringPreparing() throws Exception {
        Member owner = saveMember("recommendation-restriction-owner", "제한방장");
        Member member = saveMember("recommendation-restriction-member", "제한멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "제한 추천 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        Ingredient pork = saveIngredient("pork", "돼지고기", 1);
        MenuItem porkCutlet = saveMenu("pork-cutlet", "돈까스");
        MenuItem bibimbap = saveMenu("bibimbap", "비빔밥");
        saveMenuIngredient(porkCutlet, pork);
        saveTasteProfile(member, new AttributeCategory[]{}, new Ingredient[]{pork}, new MenuItem[]{});

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(GroupRecommendationStatus.PREPARING.name()))
                .andExpect(jsonPath("$.data.candidates.length()").value(0));

        assertThat(groupRecommendationCandidateRepository.findAll()).isEmpty();
        assertThat(menuItemRepository.findAll())
                .extracting(MenuItem::getId)
                .contains(porkCutlet.getId(), bibimbap.getId());
    }

    @Test
    @DisplayName("그룹 추천 준비 상태 조회는 현재 활성 멤버별 READY 상태와 진행률을 반환한다")
    void getGroupRecommendationReadinessReturnsActiveMemberReadiness() throws Exception {
        Member owner = saveMember("readiness-owner", "준비조회방장");
        Member member = saveMember("readiness-member", "준비조회멤버");
        Member waitingMember = saveMember("readiness-waiting", "준비조회대기");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "준비 조회 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                waitingMember,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        GroupRecommendation recommendation = groupRecommendationRepository.save(GroupRecommendation.preparing(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        groupRecommendationReadinessRepository.save(new GroupRecommendationReadiness(recommendation, owner));
        GroupRecommendationReadiness canceledReadiness =
                new GroupRecommendationReadiness(recommendation, member);
        canceledReadiness.cancel();
        groupRecommendationReadinessRepository.save(canceledReadiness);

        mockMvc.perform(get("/api/v1/groups/{groupId}/recommendations/{sessionId}/readiness",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(recommendation.getId()))
                .andExpect(jsonPath("$.data.status").value(GroupRecommendationStatus.PREPARING.name()))
                .andExpect(jsonPath("$.data.progress.totalMemberCount").value(3))
                .andExpect(jsonPath("$.data.progress.readyMemberCount").value(1))
                .andExpect(jsonPath("$.data.progress.allReady").value(false))
                .andExpect(jsonPath("$.data.members.length()").value(3))
                .andExpect(jsonPath("$.data.members[0].memberId").value(owner.getId()))
                .andExpect(jsonPath("$.data.members[0].ready").value(true))
                .andExpect(jsonPath("$.data.members[0].readinessStatus").doesNotExist())
                .andExpect(jsonPath("$.data.members[0].readinessUpdatedAt").doesNotExist())
                .andExpect(jsonPath("$.data.members[1].memberId").value(member.getId()))
                .andExpect(jsonPath("$.data.members[1].ready").value(false))
                .andExpect(jsonPath("$.data.members[1].readinessStatus").doesNotExist())
                .andExpect(jsonPath("$.data.members[1].readinessUpdatedAt").doesNotExist())
                .andExpect(jsonPath("$.data.members[2].memberId").value(waitingMember.getId()))
                .andExpect(jsonPath("$.data.members[2].ready").value(false))
                .andExpect(jsonPath("$.data.members[2].readinessStatus").doesNotExist())
                .andExpect(jsonPath("$.data.members[2].readinessUpdatedAt").doesNotExist());
    }

    @Test
    @DisplayName("그룹 추천 준비 상태 조회는 비멤버이면 거절한다")
    void getGroupRecommendationReadinessFailsForNonMember() throws Exception {
        Member owner = saveMember("readiness-access-owner", "준비조회접근방장");
        Member other = saveMember("readiness-access-other", "준비조회접근없음");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "준비 조회 접근 그룹");
        GroupRecommendation recommendation = groupRecommendationRepository.save(GroupRecommendation.preparing(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(get("/api/v1/groups/{groupId}/recommendations/{sessionId}/readiness",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(other))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GROUP_ACCESS_DENIED"));
    }

    @Test
    @DisplayName("그룹 추천 준비 상태 조회는 다른 그룹 추천이면 찾을 수 없음으로 처리한다")
    void getGroupRecommendationReadinessFailsWhenRecommendationDoesNotBelongToGroup() throws Exception {
        Member owner = saveMember("readiness-other-owner", "준비조회다른방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "준비 조회 대상 그룹");
        GroupRoom otherGroupRoom = saveGroupOwnedBy(owner, "준비 조회 다른 그룹");
        GroupRecommendation recommendation = groupRecommendationRepository.save(GroupRecommendation.preparing(
                otherGroupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(get("/api/v1/groups/{groupId}/recommendations/{sessionId}/readiness",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_NOT_FOUND"));
    }

    @Test
    @DisplayName("그룹 추천 준비 완료는 현재 회원을 READY로 저장하고 아직 전원이 아니면 PREPARING을 유지한다")
    void readyGroupRecommendationMarksMemberReadyBeforeAllMembersReady() throws Exception {
        Member owner = saveMember("ready-owner", "준비방장");
        Member member = saveMember("ready-member", "준비멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "준비 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        GroupRecommendation recommendation = groupRecommendationRepository.save(GroupRecommendation.preparing(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/ready",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(recommendation.getId()))
                .andExpect(jsonPath("$.data.status").value(GroupRecommendationStatus.PREPARING.name()))
                .andExpect(jsonPath("$.data.readiness.totalMemberCount").value(2))
                .andExpect(jsonPath("$.data.readiness.readyMemberCount").value(1))
                .andExpect(jsonPath("$.data.readiness.allReady").value(false))
                .andExpect(jsonPath("$.data.candidates.length()").value(0));

        List<GroupRecommendationReadiness> readinesses = groupRecommendationReadinessRepository.findAll();
        assertThat(readinesses).hasSize(1);
        assertThat(readinesses.getFirst().getMember().getId()).isEqualTo(member.getId());
        assertThat(readinesses.getFirst().getStatus()).isEqualTo(GroupRecommendationReadinessStatus.READY);
        assertThat(groupRecommendationRepository.findById(recommendation.getId()).orElseThrow().getStatus())
                .isEqualTo(GroupRecommendationStatus.PREPARING);
    }

    @Test
    @DisplayName("그룹 추천 준비 완료는 중복 호출되어도 READY 상태를 유지한다")
    void readyGroupRecommendationIsIdempotentForSameMember() throws Exception {
        Member owner = saveMember("ready-idempotent-owner", "준비중복방장");
        Member member = saveMember("ready-idempotent-member", "준비중복멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "준비 중복 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        GroupRecommendation recommendation = groupRecommendationRepository.save(GroupRecommendation.preparing(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/ready",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(GroupRecommendationStatus.PREPARING.name()))
                .andExpect(jsonPath("$.data.readiness.readyMemberCount").value(1));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/ready",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(GroupRecommendationStatus.PREPARING.name()))
                .andExpect(jsonPath("$.data.readiness.readyMemberCount").value(1));

        assertThat(groupRecommendationReadinessRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("마지막 그룹원이 준비 완료하면 후보를 생성하고 그룹 추천을 OPEN으로 전환한다")
    void readyGroupRecommendationOpensAndCreatesCandidatesWhenAllMembersReady() throws Exception {
        Member owner = saveMember("ready-open-owner", "준비오픈방장");
        Member member = saveMember("ready-open-member", "준비오픈멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "준비 오픈 그룹");
        groupLocationRepository.save(new GroupLocation(
                groupRoom,
                new BigDecimal("37.498095"),
                new BigDecimal("127.027610"),
                1000,
                "서울 강남구 테헤란로 123"
        ));
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        saveMenu("ready-open-first", "준비오픈첫번째");
        saveMenu("ready-open-second", "준비오픈두번째");
        GroupRecommendation recommendation = groupRecommendationRepository.save(GroupRecommendation.preparing(
                groupRoom,
                null,
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/ready",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(GroupRecommendationStatus.PREPARING.name()));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/ready",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(GroupRecommendationStatus.OPEN.name()))
                .andExpect(jsonPath("$.data.readiness.totalMemberCount").value(2))
                .andExpect(jsonPath("$.data.readiness.readyMemberCount").value(2))
                .andExpect(jsonPath("$.data.readiness.allReady").value(true))
                .andExpect(jsonPath("$.data.candidates.length()").value(2));

        GroupRecommendation openedRecommendation =
                groupRecommendationRepository.findById(recommendation.getId()).orElseThrow();
        assertThat(openedRecommendation.getStatus()).isEqualTo(GroupRecommendationStatus.OPEN);
        assertThat(openedRecommendation.getContextJson()).contains("37.498095");
        assertThat(openedRecommendation.getContextJson()).contains("127.027610");
        assertThat(openedRecommendation.getContextJson()).contains("1000");
        assertThat(openedRecommendation.getContextJson()).contains("서울 강남구 테헤란로 123");
        assertThat(groupRecommendationCandidateRepository
                .findAllByGroupRecommendationIdOrderByRankNoAsc(recommendation.getId()))
                .hasSize(2);
    }

    @Test
    @DisplayName("그룹 추천 준비 완료는 PREPARING 상태가 아니면 실패한다")
    void readyGroupRecommendationFailsWhenRecommendationIsNotPreparing() throws Exception {
        Member owner = saveMember("ready-not-preparing-owner", "준비상태아님방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "준비 상태 아님 그룹");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/ready",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_NOT_PREPARING"));
    }

    @Test
    @DisplayName("그룹 추천 준비 완료는 비멤버이면 거절한다")
    void readyGroupRecommendationFailsForNonMember() throws Exception {
        Member owner = saveMember("ready-access-owner", "준비접근방장");
        Member other = saveMember("ready-access-other", "준비접근없음");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "준비 접근 그룹");
        GroupRecommendation recommendation = groupRecommendationRepository.save(GroupRecommendation.preparing(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/ready",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(other))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GROUP_ACCESS_DENIED"));
    }

    @Test
    @DisplayName("그룹 추천 재요청은 MVP 제외 API이므로 410으로 거절한다")
    void rerollGroupRecommendationReturnsGone() throws Exception {
        Member owner = saveMember("group-reroll-owner", "재요청방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "재요청 그룹");
        MenuItem firstMenu = saveMenu("reroll-first", "첫번째메뉴");
        MenuItem secondMenu = saveMenu("reroll-second", "두번째메뉴");
        GroupRecommendation sourceRecommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        groupRecommendationCandidateRepository.save(new GroupRecommendationCandidate(
                sourceRecommendation,
                firstMenu,
                1,
                10.0,
                "{}"
        ));
        groupRecommendationCandidateRepository.save(new GroupRecommendationCandidate(
                sourceRecommendation,
                secondMenu,
                2,
                5.0,
                "{}"
        ));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/reroll",
                        groupRoom.getId(),
                        sourceRecommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rerollType": "NOT_SATISFIED",
                                  "contextJson": {
                                    "mealTime": "LUNCH"
                                  }
                                }
                                """))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.status").value(410))
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_REROLL_DISABLED"));

        GroupRecommendation savedSourceRecommendation =
                groupRecommendationRepository.findById(sourceRecommendation.getId()).orElseThrow();

        assertThat(savedSourceRecommendation.getStatus()).isEqualTo(GroupRecommendationStatus.OPEN);
        assertThat(savedSourceRecommendation.getEndedAt()).isNull();
        assertThat(groupMenuActionRepository.count()).isZero();
        assertThat(groupRecommendationRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("입력 변경 그룹 추천 재요청도 410으로 거절한다")
    void rerollGroupRecommendationWithInputChangedReturnsGone() throws Exception {
        Member owner = saveMember("group-reroll-input-owner", "입력변경방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "입력 변경 그룹");
        saveMenu("input-first", "입력첫번째");
        saveMenu("input-second", "입력두번째");
        GroupRecommendation sourceRecommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/reroll",
                        groupRoom.getId(),
                        sourceRecommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rerollType": "INPUT_CHANGED",
                                  "contextJson": {
                                    "mealTime": "DINNER"
                                  }
                                }
                                """))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_REROLL_DISABLED"));

        GroupRecommendation savedSourceRecommendation =
                groupRecommendationRepository.findById(sourceRecommendation.getId()).orElseThrow();

        assertThat(savedSourceRecommendation.getStatus())
                .isEqualTo(GroupRecommendationStatus.OPEN);
        assertThat(savedSourceRecommendation.getEndedAt()).isNull();
        assertThat(groupMenuActionRepository.count()).isZero();
        assertThat(groupRecommendationRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("그룹 추천 재요청은 OWNER가 아닌 활성 멤버여도 410으로 먼저 거절한다")
    void rerollGroupRecommendationReturnsGoneForNonOwnerMember() throws Exception {
        Member owner = saveMember("group-reroll-forbidden-owner", "재요청권한방장");
        Member member = saveMember("group-reroll-forbidden-member", "재요청권한멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "재요청 권한 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        GroupRecommendation sourceRecommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/reroll",
                        groupRoom.getId(),
                        sourceRecommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rerollType": "INPUT_CHANGED",
                                  "contextJson": {}
                                }
                                """))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_REROLL_DISABLED"));

        assertThat(groupRecommendationRepository.findById(sourceRecommendation.getId()).orElseThrow().getStatus())
                .isEqualTo(GroupRecommendationStatus.OPEN);
    }

    @Test
    @DisplayName("그룹 추천 재요청은 열린 상태 여부 검증 전 410으로 거절한다")
    void rerollGroupRecommendationReturnsGoneBeforeStatusValidation() throws Exception {
        Member owner = saveMember("group-reroll-closed-owner", "재요청종료방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "재요청 종료 그룹");
        GroupRecommendation sourceRecommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        sourceRecommendation.rerollWithoutSkip(LocalDateTime.now());
        groupRecommendationRepository.save(sourceRecommendation);

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/reroll",
                        groupRoom.getId(),
                        sourceRecommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rerollType": "INPUT_CHANGED",
                                  "contextJson": {}
                                }
                                """))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_REROLL_DISABLED"));
    }

    @Test
    @DisplayName("그룹 추천 상세 조회는 후보와 투표 진행률을 실제 저장값으로 반환한다")
    void getGroupRecommendationReturnsStoredSession() throws Exception {
        Member owner = saveMember("recommendation-view-owner", "조회방장");
        Member member = saveMember("recommendation-view-member", "조회멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "추천 조회 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        MenuItem bibimbap = saveMenu("view-bibimbap", "비빔밥");
        MenuItem gimbap = saveMenu("view-gimbap", "김밥");
        String contextJson =
                "{\"latitude\":37.498095,\"longitude\":127.027610,\"radiusMeters\":1000,\"address\":\"서울 강남구 테헤란로 123\"}";
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                contextJson,
                LocalDateTime.now()
        ));
        GroupRecommendationCandidate firstCandidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, bibimbap, 1, 70.0, "{}")
        );
        GroupRecommendationCandidate secondCandidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, gimbap, 2, 30.0, "{}")
        );
        groupRecommendationVoteRepository.save(new GroupRecommendationVote(recommendation, firstCandidate, owner));

        mockMvc.perform(get("/api/v1/groups/{groupId}/recommendations/{sessionId}",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(recommendation.getId()))
                .andExpect(jsonPath("$.data.status").value(GroupRecommendationStatus.OPEN.name()))
                .andExpect(jsonPath("$.data.contextJson").value(contextJson))
                .andExpect(jsonPath("$.data.readiness").value(nullValue()))
                .andExpect(jsonPath("$.data.candidates.length()").value(2))
                .andExpect(jsonPath("$.data.candidates[0].candidateId").value(firstCandidate.getId()))
                .andExpect(jsonPath("$.data.candidates[0].menuId").value(bibimbap.getId()))
                .andExpect(jsonPath("$.data.candidates[0].menuName").value("비빔밥"))
                .andExpect(jsonPath("$.data.candidates[0].rankNo").value(1))
                .andExpect(jsonPath("$.data.candidates[0].score").value(70.0))
                .andExpect(jsonPath("$.data.candidates[0].voteCount").value(1))
                .andExpect(jsonPath("$.data.candidates[1].candidateId").value(secondCandidate.getId()))
                .andExpect(jsonPath("$.data.candidates[1].voteCount").value(0))
                .andExpect(jsonPath("$.data.voteProgress.totalMemberCount").value(2))
                .andExpect(jsonPath("$.data.voteProgress.votedMemberCount").value(1))
                .andExpect(jsonPath("$.data.myVote").doesNotExist())
                .andExpect(jsonPath("$.data.memberVotes.length()").value(2))
                .andExpect(jsonPath("$.data.memberVotes[0].memberId").value(owner.getId()))
                .andExpect(jsonPath("$.data.memberVotes[0].nickname").value("조회방장"))
                .andExpect(jsonPath("$.data.memberVotes[0].role").value(GroupMemberRole.OWNER.name()))
                .andExpect(jsonPath("$.data.memberVotes[0].isMe").value(false))
                .andExpect(jsonPath("$.data.memberVotes[0].voted").value(true))
                .andExpect(jsonPath("$.data.memberVotes[0].candidateId").value(firstCandidate.getId()))
                .andExpect(jsonPath("$.data.memberVotes[1].memberId").value(member.getId()))
                .andExpect(jsonPath("$.data.memberVotes[1].nickname").value("조회멤버"))
                .andExpect(jsonPath("$.data.memberVotes[1].role").value(GroupMemberRole.MEMBER.name()))
                .andExpect(jsonPath("$.data.memberVotes[1].isMe").value(true))
                .andExpect(jsonPath("$.data.memberVotes[1].voted").value(false))
                .andExpect(jsonPath("$.data.memberVotes[1].candidateId").value(nullValue()))
                .andExpect(jsonPath("$.data.finalCandidate").value(nullValue()))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty());

        mockMvc.perform(get("/api/v1/groups/{groupId}/recommendations/{sessionId}",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberVotes[0].isMe").value(true))
                .andExpect(jsonPath("$.data.memberVotes[0].voted").value(true))
                .andExpect(jsonPath("$.data.memberVotes[0].candidateId").value(firstCandidate.getId()));
    }

    @Test
    @DisplayName("그룹 추천 상세 조회는 준비 세션이면 readiness 진행률을 반환하고 투표 진행률은 반환하지 않는다")
    void getGroupRecommendationReturnsReadinessForPreparingSession() throws Exception {
        Member owner = saveMember("recommendation-preparing-view-owner", "준비상세방장");
        Member member = saveMember("recommendation-preparing-view-member", "준비상세멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "준비 상세 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        GroupRecommendation recommendation = groupRecommendationRepository.save(GroupRecommendation.preparing(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        groupRecommendationReadinessRepository.save(new GroupRecommendationReadiness(recommendation, owner));

        mockMvc.perform(get("/api/v1/groups/{groupId}/recommendations/{sessionId}",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(recommendation.getId()))
                .andExpect(jsonPath("$.data.status").value(GroupRecommendationStatus.PREPARING.name()))
                .andExpect(jsonPath("$.data.readiness.totalMemberCount").value(2))
                .andExpect(jsonPath("$.data.readiness.readyMemberCount").value(1))
                .andExpect(jsonPath("$.data.readiness.allReady").value(false))
                .andExpect(jsonPath("$.data.candidates.length()").value(0))
                .andExpect(jsonPath("$.data.voteProgress").value(nullValue()))
                .andExpect(jsonPath("$.data.myVote").doesNotExist())
                .andExpect(jsonPath("$.data.memberVotes.length()").value(0))
                .andExpect(jsonPath("$.data.finalCandidate").value(nullValue()));
    }

    @Test
    @DisplayName("그룹 추천 후보 목록 조회는 후보별 투표 수를 반환한다")
    void getGroupRecommendationCandidatesReturnsVoteCounts() throws Exception {
        Member owner = saveMember("recommendation-candidates-owner", "후보방장");
        Member member = saveMember("recommendation-candidates-member", "후보멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "후보 조회 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        MenuItem ramen = saveMenu("candidate-ramen", "라멘");
        MenuItem udon = saveMenu("candidate-udon", "우동");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        GroupRecommendationCandidate firstCandidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, ramen, 1, 10.0, "{}")
        );
        GroupRecommendationCandidate secondCandidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, udon, 2, 5.0, "{}")
        );
        groupRecommendationVoteRepository.save(new GroupRecommendationVote(recommendation, secondCandidate, owner));
        groupRecommendationVoteRepository.save(new GroupRecommendationVote(recommendation, secondCandidate, member));

        mockMvc.perform(get("/api/v1/groups/{groupId}/recommendations/{sessionId}/candidates",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(recommendation.getId()))
                .andExpect(jsonPath("$.data.candidates.length()").value(2))
                .andExpect(jsonPath("$.data.candidates[0].candidateId").value(firstCandidate.getId()))
                .andExpect(jsonPath("$.data.candidates[0].voteCount").value(0))
                .andExpect(jsonPath("$.data.candidates[1].candidateId").value(secondCandidate.getId()))
                .andExpect(jsonPath("$.data.candidates[1].voteCount").value(2));
    }

    @Test
    @DisplayName("그룹 추천 후보 목록 조회는 PREPARING 세션이면 거절한다")
    void getGroupRecommendationCandidatesFailsForPreparingRecommendation() throws Exception {
        Member owner = saveMember("recommendation-candidates-preparing-owner", "준비후보방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "준비 후보 조회 그룹");
        GroupRecommendation recommendation = groupRecommendationRepository.save(GroupRecommendation.preparing(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(get("/api/v1/groups/{groupId}/recommendations/{sessionId}/candidates",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_NOT_OPEN"));
    }

    @Test
    @DisplayName("그룹 추천 요청 목록 조회는 그룹별 추천 summary를 최신순 페이지로 반환한다")
    void getGroupRecommendationsReturnsSummariesByGroup() throws Exception {
        Member owner = saveMember("recommendation-list-owner", "목록방장");
        Member member = saveMember("recommendation-list-member", "목록멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "추천 목록 그룹");
        GroupRoom otherGroupRoom = saveGroupOwnedBy(owner, "다른 추천 목록 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        GroupRecommendation oldRecommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now().minusMinutes(30)
        ));
        oldRecommendation.rerollWithoutSkip(LocalDateTime.now().minusMinutes(20));
        groupRecommendationRepository.save(oldRecommendation);
        GroupRecommendation latestRecommendation = groupRecommendationRepository.save(GroupRecommendation.preparing(
                groupRoom,
                "{}",
                LocalDateTime.now().minusMinutes(10)
        ));
        groupRecommendationRepository.save(new GroupRecommendation(
                otherGroupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(get("/api/v1/groups/{groupId}/recommendations", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].sessionId").value(latestRecommendation.getId()))
                .andExpect(jsonPath("$.data.content[0].status").value(GroupRecommendationStatus.PREPARING.name()))
                .andExpect(jsonPath("$.data.content[0].startedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].endedAt").value(nullValue()))
                .andExpect(jsonPath("$.data.content[0].finalCandidate").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].finalMenuName").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].voteProgress").doesNotExist())
                .andExpect(jsonPath("$.data.content[1].sessionId").value(oldRecommendation.getId()))
                .andExpect(jsonPath("$.data.content[1].status")
                        .value(GroupRecommendationStatus.REROLLED_WITHOUT_SKIP.name()))
                .andExpect(jsonPath("$.data.content[1].endedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(2))
                .andExpect(jsonPath("$.data.pageInfo.totalPages").value(1));
    }

    @Test
    @DisplayName("그룹 추천 요청 목록 조회는 비멤버이면 거절한다")
    void getGroupRecommendationsFailsForNonMember() throws Exception {
        Member owner = saveMember("recommendation-list-denied-owner", "목록거절방장");
        Member other = saveMember("recommendation-list-denied-other", "목록거절비멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "목록 거절 그룹");
        groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(get("/api/v1/groups/{groupId}/recommendations", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(other))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GROUP_ACCESS_DENIED"));
    }

    @Test
    @DisplayName("그룹 추천 요청 목록 조회는 삭제된 그룹이면 찾을 수 없음으로 처리한다")
    void getGroupRecommendationsFailsForDeletedGroup() throws Exception {
        Member owner = saveMember("recommendation-list-deleted-owner", "목록삭제방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "목록 삭제 그룹");
        groupRoom.delete();
        groupRoomRepository.save(groupRoom);

        mockMvc.perform(get("/api/v1/groups/{groupId}/recommendations", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("GROUP_NOT_FOUND"));
    }

    @Test
    @DisplayName("그룹 추천 요청 목록 조회는 페이지 크기가 허용 범위를 넘으면 실패한다")
    void getGroupRecommendationsFailsWithInvalidPageSize() throws Exception {
        Member owner = saveMember("recommendation-list-invalid-page-owner", "목록페이지방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "목록 페이지 그룹");

        mockMvc.perform(get("/api/v1/groups/{groupId}/recommendations", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("그룹 추천 투표는 활성 멤버의 후보 선택을 저장한다")
    void voteGroupRecommendationStoresMemberVote() throws Exception {
        Member owner = saveMember("recommendation-vote-owner", "투표방장");
        Member member = saveMember("recommendation-vote-member", "투표멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "투표 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        MenuItem menuItem = saveMenu("vote-menu", "투표메뉴");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        GroupRecommendationCandidate candidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, menuItem, 1, 10.0, "{}")
        );

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/votes",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "candidateId": %d
                                }
                                """.formatted(candidate.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.voteId").isNumber())
                .andExpect(jsonPath("$.data.candidateId").value(candidate.getId()))
                .andExpect(jsonPath("$.data.voteValue").doesNotExist())
                .andExpect(jsonPath("$.data.votedAt").isNotEmpty());

        List<GroupRecommendationVote> votes = groupRecommendationVoteRepository.findAll();
        assertThat(votes).hasSize(1);
        assertThat(votes.getFirst().getGroupRecommendation().getId()).isEqualTo(recommendation.getId());
        assertThat(votes.getFirst().getCandidate().getId()).isEqualTo(candidate.getId());
        assertThat(votes.getFirst().getMember().getId()).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("그룹 추천 투표는 같은 회원의 기존 투표 후보를 변경한다")
    void voteGroupRecommendationChangesExistingMemberVote() throws Exception {
        Member owner = saveMember("recommendation-revote-owner", "재투표방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "재투표 그룹");
        MenuItem firstMenuItem = saveMenu("revote-first-menu", "재투표첫메뉴");
        MenuItem secondMenuItem = saveMenu("revote-second-menu", "재투표둘째메뉴");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        GroupRecommendationCandidate firstCandidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, firstMenuItem, 1, 10.0, "{}")
        );
        GroupRecommendationCandidate secondCandidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, secondMenuItem, 2, 9.0, "{}")
        );
        GroupRecommendationVote existingVote = groupRecommendationVoteRepository.save(new GroupRecommendationVote(
                recommendation,
                firstCandidate,
                owner
        ));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/votes",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "candidateId": %d
                                }
                                """.formatted(secondCandidate.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.voteId").value(existingVote.getId()))
                .andExpect(jsonPath("$.data.candidateId").value(secondCandidate.getId()))
                .andExpect(jsonPath("$.data.votedAt").isNotEmpty());

        assertThat(groupRecommendationVoteRepository.count()).isEqualTo(1);
        GroupRecommendationVote updatedVote = groupRecommendationVoteRepository.findAll().getFirst();
        assertThat(updatedVote.getId()).isEqualTo(existingVote.getId());
        assertThat(updatedVote.getCandidate().getId()).isEqualTo(secondCandidate.getId());
    }

    @Test
    @DisplayName("그룹 추천 투표는 같은 후보 재투표를 성공으로 처리한다")
    void voteGroupRecommendationIsIdempotentForSameCandidate() throws Exception {
        Member owner = saveMember("recommendation-idempotent-vote-owner", "재투표동일방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "동일 후보 재투표 그룹");
        MenuItem menuItem = saveMenu("idempotent-vote-menu", "동일재투표메뉴");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        GroupRecommendationCandidate candidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, menuItem, 1, 10.0, "{}")
        );
        GroupRecommendationVote existingVote = groupRecommendationVoteRepository.save(new GroupRecommendationVote(
                recommendation,
                candidate,
                owner
        ));

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/votes",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "candidateId": %d
                                }
                                """.formatted(candidate.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.voteId").value(existingVote.getId()))
                .andExpect(jsonPath("$.data.candidateId").value(candidate.getId()))
                .andExpect(jsonPath("$.data.votedAt").isNotEmpty());

        assertThat(groupRecommendationVoteRepository.count()).isEqualTo(1);
        assertThat(groupRecommendationVoteRepository.findAll().getFirst().getCandidate().getId())
                .isEqualTo(candidate.getId());
    }

    @Test
    @DisplayName("그룹 추천 투표는 다른 추천의 후보이면 찾을 수 없음으로 처리한다")
    void voteGroupRecommendationFailsWhenCandidateBelongsToOtherRecommendation() throws Exception {
        Member owner = saveMember("recommendation-wrong-candidate-owner", "후보다름방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "후보 소속 그룹");
        MenuItem menuItem = saveMenu("wrong-candidate-menu", "후보다름메뉴");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        GroupRecommendation otherRecommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        GroupRecommendationCandidate otherCandidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(otherRecommendation, menuItem, 1, 10.0, "{}")
        );

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/votes",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "candidateId": %d
                                }
                                """.formatted(otherCandidate.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_CANDIDATE_NOT_FOUND"));

        assertThat(groupRecommendationVoteRepository.count()).isZero();
    }

    @Test
    @DisplayName("그룹 추천 투표는 열린 상태가 아니면 거절한다")
    void voteGroupRecommendationFailsForNotOpenRecommendation() throws Exception {
        Member owner = saveMember("recommendation-vote-closed-owner", "투표종료방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "투표 종료 그룹");
        MenuItem menuItem = saveMenu("vote-closed-menu", "투표종료메뉴");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        GroupRecommendationCandidate candidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, menuItem, 1, 10.0, "{}")
        );
        recommendation.rerollWithoutSkip(LocalDateTime.now());
        groupRecommendationRepository.save(recommendation);

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/votes",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "candidateId": %d
                                }
                                """.formatted(candidate.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_NOT_OPEN"));

        assertThat(groupRecommendationVoteRepository.count()).isZero();
    }

    @Test
    @DisplayName("그룹 추천 투표는 활성 멤버가 아니면 거절한다")
    void voteGroupRecommendationFailsForNonMember() throws Exception {
        Member owner = saveMember("recommendation-vote-access-owner", "투표접근방장");
        Member other = saveMember("recommendation-vote-access-other", "투표접근없음");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "투표 접근 그룹");
        MenuItem menuItem = saveMenu("vote-access-menu", "투표접근메뉴");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        GroupRecommendationCandidate candidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, menuItem, 1, 10.0, "{}")
        );

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/votes",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(other)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "candidateId": %d
                                }
                                """.formatted(candidate.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GROUP_ACCESS_DENIED"));

        assertThat(groupRecommendationVoteRepository.count()).isZero();
    }

    @Test
    @DisplayName("그룹 추천 최종 확정은 OWNER가 최다 득표 후보를 저장한다")
    void finalizeGroupRecommendationSelectsMostVotedCandidateForOwner() throws Exception {
        Member owner = saveMember("recommendation-finalize-owner", "확정방장");
        Member member = saveMember("recommendation-finalize-member", "확정멤버");
        Member anotherMember = saveMember("recommendation-finalize-another", "확정멤버둘");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "확정 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(groupRoom, member, GroupMemberRole.MEMBER, LocalDateTime.now()));
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                anotherMember,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        MenuItem bibimbap = saveMenu("finalize-bibimbap", "확정비빔밥");
        MenuItem gimbap = saveMenu("finalize-gimbap", "확정김밥");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        GroupRecommendationCandidate firstCandidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, bibimbap, 1, 70.0, "{}")
        );
        GroupRecommendationCandidate secondCandidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, gimbap, 2, 60.0, "{}")
        );
        groupRecommendationVoteRepository.save(new GroupRecommendationVote(recommendation, firstCandidate, owner));
        groupRecommendationVoteRepository.save(new GroupRecommendationVote(recommendation, secondCandidate, member));
        groupRecommendationVoteRepository.save(new GroupRecommendationVote(recommendation, secondCandidate,
                anotherMember));

        mockMvc.perform(patch("/api/v1/groups/{groupId}/recommendations/{sessionId}/finalize",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(recommendation.getId()))
                .andExpect(jsonPath("$.data.status").value(GroupRecommendationStatus.FINALIZED.name()))
                .andExpect(jsonPath("$.data.finalCandidate.candidateId").value(secondCandidate.getId()))
                .andExpect(jsonPath("$.data.finalCandidate.menuId").value(gimbap.getId()))
                .andExpect(jsonPath("$.data.finalCandidate.rankNo").value(2))
                .andExpect(jsonPath("$.data.finalCandidate.voteCount").value(2))
                .andExpect(jsonPath("$.data.finalizedAt").isNotEmpty());

        GroupRecommendation finalizedRecommendation =
                groupRecommendationRepository.findById(recommendation.getId()).orElseThrow();
        assertThat(finalizedRecommendation.getStatus()).isEqualTo(GroupRecommendationStatus.FINALIZED);
        assertThat(finalizedRecommendation.getSelectedCandidate().getId()).isEqualTo(secondCandidate.getId());
        assertThat(finalizedRecommendation.getEndedAt()).isNotNull();
    }

    @Test
    @DisplayName("그룹 추천 최종 확정은 동률이면 추천 순위가 높은 후보를 저장한다")
    void finalizeGroupRecommendationBreaksVoteTieByRankNo() throws Exception {
        Member owner = saveMember("recommendation-finalize-tie-owner", "동률확정방장");
        Member member = saveMember("recommendation-finalize-tie-member", "동률확정멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "동률 확정 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        MenuItem firstMenu = saveMenu("finalize-tie-first", "동률첫번째");
        MenuItem secondMenu = saveMenu("finalize-tie-second", "동률두번째");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        GroupRecommendationCandidate firstCandidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, firstMenu, 1, 70.0, "{}")
        );
        GroupRecommendationCandidate secondCandidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, secondMenu, 2, 70.0, "{}")
        );
        groupRecommendationVoteRepository.save(new GroupRecommendationVote(recommendation, firstCandidate, owner));
        groupRecommendationVoteRepository.save(new GroupRecommendationVote(recommendation, secondCandidate, member));

        mockMvc.perform(patch("/api/v1/groups/{groupId}/recommendations/{sessionId}/finalize",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalCandidate.candidateId").value(firstCandidate.getId()))
                .andExpect(jsonPath("$.data.finalCandidate.rankNo").value(1))
                .andExpect(jsonPath("$.data.finalCandidate.voteCount").value(1));
    }

    @Test
    @DisplayName("그룹 추천 최종 확정은 투표가 없으면 1순위 후보를 저장한다")
    void finalizeGroupRecommendationSelectsRankOneWhenNoVotes() throws Exception {
        Member owner = saveMember("recommendation-finalize-no-vote-owner", "무투표확정방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "무투표 확정 그룹");
        MenuItem firstMenu = saveMenu("finalize-no-vote-first", "무투표첫번째");
        MenuItem secondMenu = saveMenu("finalize-no-vote-second", "무투표두번째");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        GroupRecommendationCandidate firstCandidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, firstMenu, 1, 70.0, "{}")
        );
        groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, secondMenu, 2, 60.0, "{}")
        );

        mockMvc.perform(patch("/api/v1/groups/{groupId}/recommendations/{sessionId}/finalize",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalCandidate.candidateId").value(firstCandidate.getId()))
                .andExpect(jsonPath("$.data.finalCandidate.voteCount").value(0));
    }

    @Test
    @DisplayName("그룹 추천 최종 확정은 OWNER가 아닌 활성 멤버이면 거절한다")
    void finalizeGroupRecommendationFailsForNonOwnerMember() throws Exception {
        Member owner = saveMember("recommendation-finalize-forbidden-owner", "확정권한방장");
        Member member = saveMember("recommendation-finalize-forbidden-member", "확정권한멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "확정 권한 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        MenuItem menuItem = saveMenu("finalize-forbidden-menu", "확정권한메뉴");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, menuItem, 1, 10.0, "{}")
        );

        mockMvc.perform(patch("/api/v1/groups/{groupId}/recommendations/{sessionId}/finalize",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_FINALIZE_FORBIDDEN"));

        assertThat(groupRecommendationRepository.findById(recommendation.getId()).orElseThrow().getStatus())
                .isEqualTo(GroupRecommendationStatus.OPEN);
    }

    @Test
    @DisplayName("그룹 추천 최종 확정은 열린 상태가 아니면 거절한다")
    void finalizeGroupRecommendationFailsForNotOpenRecommendation() throws Exception {
        Member owner = saveMember("recommendation-finalize-closed-owner", "확정종료방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "확정 종료 그룹");
        MenuItem menuItem = saveMenu("finalize-closed-menu", "확정종료메뉴");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        GroupRecommendationCandidate candidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, menuItem, 1, 10.0, "{}")
        );
        recommendation.finalizeWith(candidate, LocalDateTime.now());
        groupRecommendationRepository.save(recommendation);

        mockMvc.perform(patch("/api/v1/groups/{groupId}/recommendations/{sessionId}/finalize",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_NOT_OPEN"));
    }

    @Test
    @DisplayName("그룹 추천 최종 확정은 후보가 없으면 거절한다")
    void finalizeGroupRecommendationFailsWhenNoCandidates() throws Exception {
        Member owner = saveMember("recommendation-finalize-empty-owner", "후보없음방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "후보 없음 그룹");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(patch("/api/v1/groups/{groupId}/recommendations/{sessionId}/finalize",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_NO_CANDIDATES"));

        assertThat(groupRecommendationRepository.findById(recommendation.getId()).orElseThrow().getStatus())
                .isEqualTo(GroupRecommendationStatus.OPEN);
    }

    @Test
    @DisplayName("확정된 그룹 추천에는 더 이상 투표할 수 없다")
    void voteGroupRecommendationFailsAfterFinalized() throws Exception {
        Member owner = saveMember("recommendation-vote-finalized-owner", "확정후투표방장");
        Member member = saveMember("recommendation-vote-finalized-member", "확정후투표멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "확정 후 투표 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        MenuItem menuItem = saveMenu("vote-finalized-menu", "확정후투표메뉴");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        GroupRecommendationCandidate candidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, menuItem, 1, 10.0, "{}")
        );
        recommendation.finalizeWith(candidate, LocalDateTime.now());
        groupRecommendationRepository.save(recommendation);

        mockMvc.perform(post("/api/v1/groups/{groupId}/recommendations/{sessionId}/votes",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "candidateId": %d
                                }
                                """.formatted(candidate.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_NOT_OPEN"));

        assertThat(groupRecommendationVoteRepository.count()).isZero();
    }

    @Test
    @DisplayName("그룹 추천 조회는 활성 멤버가 아니면 거절한다")
    void getGroupRecommendationFailsForNonMember() throws Exception {
        Member owner = saveMember("recommendation-view-denied-owner", "조회거절방장");
        Member other = saveMember("recommendation-view-denied-other", "조회거절비멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "조회 거절 그룹");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(get("/api/v1/groups/{groupId}/recommendations/{sessionId}",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(other))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GROUP_ACCESS_DENIED"));
    }

    @Test
    @DisplayName("그룹 추천 조회는 추천이 해당 그룹에 속하지 않으면 찾을 수 없음으로 처리한다")
    void getGroupRecommendationFailsWhenRecommendationDoesNotBelongToGroup() throws Exception {
        Member owner = saveMember("recommendation-wrong-group-owner", "다른그룹방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "조회 대상 그룹");
        GroupRoom otherGroupRoom = saveGroupOwnedBy(owner, "다른 추천 그룹");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                otherGroupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(get("/api/v1/groups/{groupId}/recommendations/{sessionId}",
                        groupRoom.getId(),
                        recommendation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("GROUP_RECOMMENDATION_NOT_FOUND"));
    }

    @Test
    @DisplayName("그룹 상세 조회는 열린 그룹 추천이 있으면 recentlyRecommendation을 반환한다")
    void getGroupReturnsRecentlyOpenRecommendation() throws Exception {
        Member owner = saveMember("group-active-recommendation-owner", "활성추천방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "활성 추천 그룹");
        MenuItem menuItem = saveMenu("active-rec-menu", "활성추천메뉴");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        GroupRecommendationCandidate candidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, menuItem, 1, 20.0, "{}")
        );

        mockMvc.perform(get("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recentlyRecommendation.sessionId").value(recommendation.getId()))
                .andExpect(jsonPath("$.data.recentlyRecommendation.status").value(GroupRecommendationStatus.OPEN.name()))
                .andExpect(jsonPath("$.data.recentlyRecommendation.readiness").value(nullValue()))
                .andExpect(jsonPath("$.data.recentlyRecommendation.candidates[0].candidateId").value(candidate.getId()))
                .andExpect(jsonPath("$.data.recentlyRecommendation.voteProgress.totalMemberCount").value(1))
                .andExpect(jsonPath("$.data.recentlyRecommendation.voteProgress.votedMemberCount").value(0));
    }

    @Test
    @DisplayName("그룹 상세 조회는 준비 중인 그룹 추천도 recentlyRecommendation으로 반환한다")
    void getGroupReturnsPreparingRecentlyRecommendation() throws Exception {
        Member owner = saveMember("group-preparing-recommendation-owner", "준비추천방장");
        Member member = saveMember("group-preparing-recommendation-member", "준비추천멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "준비 추천 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        GroupRecommendation recommendation = groupRecommendationRepository.save(GroupRecommendation.preparing(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));
        groupRecommendationReadinessRepository.save(new GroupRecommendationReadiness(recommendation, owner));

        mockMvc.perform(get("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recentlyRecommendation.sessionId").value(recommendation.getId()))
                .andExpect(jsonPath("$.data.recentlyRecommendation.status")
                        .value(GroupRecommendationStatus.PREPARING.name()))
                .andExpect(jsonPath("$.data.recentlyRecommendation.readiness.totalMemberCount").value(2))
                .andExpect(jsonPath("$.data.recentlyRecommendation.readiness.readyMemberCount").value(1))
                .andExpect(jsonPath("$.data.recentlyRecommendation.candidates.length()").value(0))
                .andExpect(jsonPath("$.data.recentlyRecommendation.voteProgress").value(nullValue()));
    }

    @Test
    @DisplayName("그룹 상세 조회는 종료된 최신 그룹 추천 결과도 recentlyRecommendation으로 반환한다")
    void getGroupReturnsFinalizedRecentlyRecommendation() throws Exception {
        Member owner = saveMember("group-finalized-recent-owner", "확정최근방장");
        Member member = saveMember("group-finalized-recent-member", "확정최근멤버");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "확정 최근 그룹");
        groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                member,
                GroupMemberRole.MEMBER,
                LocalDateTime.now()
        ));
        MenuItem chicken = saveMenu("recent-final-chicken", "치킨");
        MenuItem gimbap = saveMenu("recent-final-gimbap", "김밥");
        GroupRecommendation recommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now().minusMinutes(10)
        ));
        GroupRecommendationCandidate firstCandidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, chicken, 1, 80.0, "{}")
        );
        GroupRecommendationCandidate secondCandidate = groupRecommendationCandidateRepository.save(
                new GroupRecommendationCandidate(recommendation, gimbap, 2, 70.0, "{}")
        );
        groupRecommendationVoteRepository.save(new GroupRecommendationVote(recommendation, firstCandidate, owner));
        groupRecommendationVoteRepository.save(new GroupRecommendationVote(recommendation, firstCandidate, member));
        recommendation.finalizeWith(firstCandidate, LocalDateTime.now().minusMinutes(5));
        groupRecommendationRepository.save(recommendation);

        mockMvc.perform(get("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recentlyRecommendation.sessionId").value(recommendation.getId()))
                .andExpect(jsonPath("$.data.recentlyRecommendation.status")
                        .value(GroupRecommendationStatus.FINALIZED.name()))
                .andExpect(jsonPath("$.data.recentlyRecommendation.readiness").value(nullValue()))
                .andExpect(jsonPath("$.data.recentlyRecommendation.candidates.length()").value(2))
                .andExpect(jsonPath("$.data.recentlyRecommendation.candidates[0].candidateId")
                        .value(firstCandidate.getId()))
                .andExpect(jsonPath("$.data.recentlyRecommendation.candidates[0].voteCount").value(2))
                .andExpect(jsonPath("$.data.recentlyRecommendation.candidates[1].candidateId")
                        .value(secondCandidate.getId()))
                .andExpect(jsonPath("$.data.recentlyRecommendation.voteProgress.totalMemberCount").value(2))
                .andExpect(jsonPath("$.data.recentlyRecommendation.voteProgress.votedMemberCount").value(2))
                .andExpect(jsonPath("$.data.recentlyRecommendation.finalCandidate.candidateId")
                        .value(firstCandidate.getId()))
                .andExpect(jsonPath("$.data.recentlyRecommendation.finalCandidate.menuName").value("치킨"));
    }

    @Test
    @DisplayName("내 그룹 목록은 최신 그룹 추천 상태를 반환한다")
    void getMyGroupsReturnsLatestRecommendationStatus() throws Exception {
        Member owner = saveMember("group-list-recommendation-owner", "목록추천방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "목록 추천 그룹");
        GroupRecommendation oldRecommendation = groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now().minusMinutes(30)
        ));
        oldRecommendation.rerollWithoutSkip(LocalDateTime.now().minusMinutes(20));
        groupRecommendationRepository.save(GroupRecommendation.preparing(
                groupRoom,
                "{}",
                LocalDateTime.now().minusMinutes(10)
        ));

        mockMvc.perform(get("/api/v1/groups")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.content[0].latestRecommendationStatus")
                        .value(GroupRecommendationStatus.PREPARING.name()));
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
        GroupRoom deletedGroup = GroupRoom.createOwnedBy("삭제된 그룹", nextInviteCode(), member);
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
        GroupRoom closedGroup = GroupRoom.createOwnedBy("닫힌 그룹", nextInviteCode(), member);
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
                .andExpect(jsonPath("$.data.members[0].isMe").value(true))
                .andExpect(jsonPath("$.data.members[1].memberId").value(activeMember.getId()))
                .andExpect(jsonPath("$.data.members[1].nickname").value("상세멤버"))
                .andExpect(jsonPath("$.data.members[1].isMe").value(false))
                .andExpect(jsonPath("$.data.recentlyRecommendation").value(nullValue()));

        mockMvc.perform(get("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(activeMember))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.members[0].memberId").value(owner.getId()))
                .andExpect(jsonPath("$.data.members[0].isMe").value(false))
                .andExpect(jsonPath("$.data.members[1].memberId").value(activeMember.getId()))
                .andExpect(jsonPath("$.data.members[1].isMe").value(true))
                .andExpect(jsonPath("$.data.recentlyRecommendation").value(nullValue()));
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
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.openGroupRecommendationId").doesNotExist());

        assertThat(groupRoomRepository.findById(groupRoom.getId()).orElseThrow().getName())
                .isEqualTo("수정 후 그룹");
    }

    @Test
    @DisplayName("그룹 수정은 열린 그룹 추천이 있어도 재요청용 추천 ID를 반환하지 않는다")
    void updateGroupDoesNotReturnOpenGroupRecommendationId() throws Exception {
        Member owner = saveMember("update-open-recommendation-owner", "열린추천수정방장");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "열린 추천 수정 그룹");
        groupRecommendationRepository.save(new GroupRecommendation(
                groupRoom,
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(patch("/api/v1/groups/{groupId}", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "latitude": 37.498095,
                                  "longitude": 127.027610,
                                  "radiusMeters": 1000,
                                  "address": "서울 강남구 테헤란로 123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupId").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.openGroupRecommendationId").doesNotExist());
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
                                  "longitude": 127.027610,
                                  "radiusMeters": 1000,
                                  "address": "서울 강남구 테헤란로 123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupId").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.name").value("위치 수정 그룹"))
                .andExpect(jsonPath("$.data.latitude").value(37.498095))
                .andExpect(jsonPath("$.data.longitude").value(127.027610))
                .andExpect(jsonPath("$.data.radiusMeters").value(1000))
                .andExpect(jsonPath("$.data.address").value("서울 강남구 테헤란로 123"))
                .andExpect(jsonPath("$.data.status").value(GroupRoomStatus.ACTIVE.name()));

        GroupRoom updatedGroup = groupRoomRepository.findById(groupRoom.getId()).orElseThrow();
        GroupLocation updatedLocation = latestGroupLocation(updatedGroup);
        assertThat(updatedGroup.getName()).isEqualTo("위치 수정 그룹");
        assertThat(updatedLocation.getLatitude()).isEqualByComparingTo("37.498095");
        assertThat(updatedLocation.getLongitude()).isEqualByComparingTo("127.027610");
        assertThat(updatedLocation.getRadiusMeters()).isEqualTo(1000);
        assertThat(updatedLocation.getAddress()).isEqualTo("서울 강남구 테헤란로 123");
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
    @DisplayName("내 그룹 초대 목록은 현재 회원이 받은 PENDING 초대를 기본 조회한다")
    void getMyInvitesReturnsPendingInvitesForCurrentTargetMember() throws Exception {
        Member owner = saveMember("my-invite-owner", "내초대방장");
        Member target = saveMember("my-invite-target", "내초대대상");
        Member otherTarget = saveMember("my-invite-other-target", "다른초대대상");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "받은 초대 그룹");
        GroupRoom otherGroupRoom = saveGroupOwnedBy(owner, "다른 대상 초대 그룹");
        GroupInvite pendingInvite = saveInvite(groupRoom, owner, target, LocalDateTime.now().plusHours(3));
        GroupInvite declinedInvite = saveInvite(groupRoom, owner, target, LocalDateTime.now().plusHours(4));
        declinedInvite.decline(LocalDateTime.now());
        groupInviteRepository.save(declinedInvite);
        saveInvite(otherGroupRoom, owner, otherTarget, LocalDateTime.now().plusHours(5));

        mockMvc.perform(get("/api/v1/groups/invites/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(target)))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].inviteId").value(pendingInvite.getId()))
                .andExpect(jsonPath("$.data.content[0].groupId").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.content[0].groupName").value("받은 초대 그룹"))
                .andExpect(jsonPath("$.data.content[0].requestMemberId").value(owner.getId()))
                .andExpect(jsonPath("$.data.content[0].requestMemberNickname").value(owner.getNickname()))
                .andExpect(jsonPath("$.data.content[0].status").value(GroupInviteStatus.PENDING.name()))
                .andExpect(jsonPath("$.data.content[0].expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1));
    }

    @Test
    @DisplayName("내 그룹 초대 목록은 상태 필터와 페이지네이션을 적용한다")
    void getMyInvitesAppliesStatusFilterAndPagination() throws Exception {
        Member owner = saveMember("my-invite-filter-owner", "초대필터방장");
        Member target = saveMember("my-invite-filter-target", "초대필터대상");
        GroupRoom firstGroup = saveGroupOwnedBy(owner, "거절 초대 1");
        GroupRoom secondGroup = saveGroupOwnedBy(owner, "거절 초대 2");
        GroupInvite firstDeclinedInvite = saveInvite(firstGroup, owner, target, LocalDateTime.now().plusHours(1));
        firstDeclinedInvite.decline(LocalDateTime.now().minusMinutes(1));
        groupInviteRepository.save(firstDeclinedInvite);
        GroupInvite secondDeclinedInvite = saveInvite(secondGroup, owner, target, LocalDateTime.now().plusHours(2));
        secondDeclinedInvite.decline(LocalDateTime.now());
        groupInviteRepository.save(secondDeclinedInvite);

        mockMvc.perform(get("/api/v1/groups/invites/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(target)))
                        .param("status", GroupInviteStatus.DECLINED.name())
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].inviteId").value(secondDeclinedInvite.getId()))
                .andExpect(jsonPath("$.data.content[0].status").value(GroupInviteStatus.DECLINED.name()))
                .andExpect(jsonPath("$.data.pageInfo.size").value(1))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(2));
    }

    @Test
    @DisplayName("그룹 초대 수락은 초대를 ACCEPTED로 닫고 신규 멤버를 ACTIVE로 추가한다")
    void respondInviteAcceptCreatesActiveMember() throws Exception {
        Member owner = saveMember("invite-accept-owner", "수락방장");
        Member target = saveMember("invite-accept-target", "수락대상");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "수락 초대 그룹");
        GroupInvite invite = saveInvite(groupRoom, owner, target, LocalDateTime.now().plusHours(1));

        mockMvc.perform(post("/api/v1/groups/invites/{inviteId}/response", invite.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(target)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "responseType": "ACCEPT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.inviteId").value(invite.getId()))
                .andExpect(jsonPath("$.data.groupId").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.inviteStatus").value(GroupInviteStatus.ACCEPTED.name()))
                .andExpect(jsonPath("$.data.memberStatus").value(GroupMemberStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.data.respondedAt").isNotEmpty());

        GroupInvite acceptedInvite = groupInviteRepository.findById(invite.getId()).orElseThrow();
        GroupRoomMember membership = groupRoomMemberRepository
                .findByRoomIdAndMemberId(groupRoom.getId(), target.getId())
                .orElseThrow();

        assertThat(acceptedInvite.getStatus()).isEqualTo(GroupInviteStatus.ACCEPTED);
        assertThat(acceptedInvite.getRespondedAt()).isNotNull();
        assertThat(membership.getRole()).isEqualTo(GroupMemberRole.MEMBER);
        assertThat(membership.getStatus()).isEqualTo(GroupMemberStatus.ACTIVE);
        assertThat(membership.getLeftAt()).isNull();
    }

    @Test
    @DisplayName("그룹 초대 수락은 LEFT 멤버의 기존 membership을 재활성화한다")
    void respondInviteAcceptReactivatesLeftMember() throws Exception {
        Member owner = saveMember("invite-rejoin-owner", "초대재입장방장");
        Member target = saveMember("invite-rejoin-target", "초대재입장대상");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "초대 재입장 그룹");
        GroupRoomMember membership = groupRoomMemberRepository.save(new GroupRoomMember(
                groupRoom,
                target,
                GroupMemberRole.MEMBER,
                LocalDateTime.now().minusDays(2)
        ));
        membership.leave(LocalDateTime.now().minusDays(1));
        groupRoomMemberRepository.save(membership);
        LocalDateTime previousJoinedAt = membership.getJoinedAt();
        GroupInvite invite = saveInvite(groupRoom, owner, target, LocalDateTime.now().plusHours(1));

        mockMvc.perform(post("/api/v1/groups/invites/{inviteId}/response", invite.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(target)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "responseType": "ACCEPT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inviteStatus").value(GroupInviteStatus.ACCEPTED.name()))
                .andExpect(jsonPath("$.data.memberStatus").value(GroupMemberStatus.ACTIVE.name()));

        GroupRoomMember rejoinedMembership = groupRoomMemberRepository
                .findByRoomIdAndMemberId(groupRoom.getId(), target.getId())
                .orElseThrow();

        assertThat(rejoinedMembership.getId()).isEqualTo(membership.getId());
        assertThat(rejoinedMembership.getStatus()).isEqualTo(GroupMemberStatus.ACTIVE);
        assertThat(rejoinedMembership.getLeftAt()).isNull();
        assertThat(rejoinedMembership.getJoinedAt()).isAfter(previousJoinedAt);
    }

    @Test
    @DisplayName("그룹 초대 거절은 초대만 DECLINED로 닫고 membership을 생성하지 않는다")
    void respondInviteDeclineClosesInviteOnly() throws Exception {
        Member owner = saveMember("invite-decline-owner", "거절방장");
        Member target = saveMember("invite-decline-target", "거절대상");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "거절 초대 그룹");
        GroupInvite invite = saveInvite(groupRoom, owner, target, LocalDateTime.now().plusHours(1));

        mockMvc.perform(post("/api/v1/groups/invites/{inviteId}/response", invite.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(target)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "responseType": "DECLINE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inviteId").value(invite.getId()))
                .andExpect(jsonPath("$.data.groupId").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.inviteStatus").value(GroupInviteStatus.DECLINED.name()))
                .andExpect(jsonPath("$.data.memberStatus").value(nullValue()))
                .andExpect(jsonPath("$.data.respondedAt").isNotEmpty());

        GroupInvite declinedInvite = groupInviteRepository.findById(invite.getId()).orElseThrow();

        assertThat(declinedInvite.getStatus()).isEqualTo(GroupInviteStatus.DECLINED);
        assertThat(declinedInvite.getRespondedAt()).isNotNull();
        assertThat(groupRoomMemberRepository.findByRoomIdAndMemberId(groupRoom.getId(), target.getId())).isEmpty();
    }

    @Test
    @DisplayName("그룹 초대 응답은 대상 회원이 아니면 거절한다")
    void respondInviteFailsForNonTargetMember() throws Exception {
        Member owner = saveMember("invite-forbidden-owner", "응답권한방장");
        Member target = saveMember("invite-forbidden-target", "응답권한대상");
        Member other = saveMember("invite-forbidden-other", "응답권한없음");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "응답 권한 그룹");
        GroupInvite invite = saveInvite(groupRoom, owner, target, LocalDateTime.now().plusHours(1));

        mockMvc.perform(post("/api/v1/groups/invites/{inviteId}/response", invite.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(other)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "responseType": "ACCEPT"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("GROUP_INVITE_RESPONSE_FORBIDDEN"));

        assertThat(groupInviteRepository.findById(invite.getId()).orElseThrow().getStatus())
                .isEqualTo(GroupInviteStatus.PENDING);
    }

    @Test
    @DisplayName("그룹 초대 응답은 만료된 PENDING 초대를 거절하고 membership을 만들지 않는다")
    void respondInviteFailsForExpiredPendingInvite() throws Exception {
        Member owner = saveMember("invite-expired-owner", "만료방장");
        Member target = saveMember("invite-expired-target", "만료대상");
        GroupRoom groupRoom = saveGroupOwnedBy(owner, "만료 초대 그룹");
        GroupInvite invite = saveInvite(groupRoom, owner, target, LocalDateTime.now().minusMinutes(1));

        mockMvc.perform(post("/api/v1/groups/invites/{inviteId}/response", invite.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(target)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "responseType": "ACCEPT"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("GROUP_INVITE_EXPIRED"));

        assertThat(groupInviteRepository.findById(invite.getId()).orElseThrow().getStatus())
                .isEqualTo(GroupInviteStatus.PENDING);
        assertThat(groupRoomMemberRepository.findByRoomIdAndMemberId(groupRoom.getId(), target.getId())).isEmpty();
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

        mockMvc.perform(post("/api/v1/groups/{groupId}/leave", groupRoom.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.groupId").value(groupRoom.getId()))
                .andExpect(jsonPath("$.data.memberStatus").value(GroupMemberStatus.LEFT.name()))
                .andExpect(jsonPath("$.data.leftAt").isNotEmpty());

        GroupRoomMember savedMembership = groupRoomMemberRepository.findById(membership.getId()).orElseThrow();

        assertThat(savedMembership.getStatus()).isEqualTo(GroupMemberStatus.LEFT);
        assertThat(savedMembership.getLeftAt()).isNotNull();
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
        return groupRoomRepository.save(GroupRoom.createOwnedBy(name, nextInviteCode(), member));
    }

    private AttributeCategory saveCategory(String code, String name, int sortOrder) {
        return attributeCategoryRepository.save(new AttributeCategory(CategoryType.FLAVOR, code, name, sortOrder));
    }

    private Ingredient saveIngredient(String code, String name, int sortOrder) {
        return ingredientRepository.save(new Ingredient(code, name, false, sortOrder));
    }

    private MenuItem saveMenu(String code, String name, AttributeCategory... categories) {
        MenuItem menuItem = menuItemRepository.save(new MenuItem(code, name, name + " 설명"));

        for (AttributeCategory category : categories) {
            menuAttributeCategoryRepository.save(new MenuAttributeCategory(menuItem, category));
        }

        return menuItem;
    }

    private void saveMenuIngredient(MenuItem menuItem, Ingredient ingredient) {
        menuIngredientRepository.save(new MenuIngredient(menuItem, ingredient));
    }

    private void saveTasteProfile(
            Member member,
            AttributeCategory[] categories,
            Ingredient[] restrictionIngredients,
            MenuItem[] dislikedMenuItems
    ) {
        MemberTasteProfile profile = memberTasteProfileRepository.save(new MemberTasteProfile(member, "v1"));

        for (AttributeCategory category : categories) {
            memberTasteProfileCategoryRepository.save(new MemberTasteProfileCategory(profile, category));
        }

        for (Ingredient ingredient : restrictionIngredients) {
            memberTasteProfileRestrictionIngredientRepository.save(
                    new MemberTasteProfileRestrictionIngredient(profile, ingredient)
            );
        }

        for (MenuItem dislikedMenuItem : dislikedMenuItems) {
            memberTasteProfileDislikedMenuItemRepository.save(
                    new MemberTasteProfileDislikedMenuItem(profile, dislikedMenuItem)
            );
        }
    }

    private String nextInviteCode() {
        return "T%07d".formatted(groupRoomRepository.count() + 1);
    }

    private GroupLocation latestGroupLocation(GroupRoom groupRoom) {
        return groupLocationRepository.findFirstByRoomIdOrderByCreatedAtDescIdDesc(groupRoom.getId()).orElseThrow();
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
