package matchuri.backend.api.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import matchuri.backend.domain.group.entity.GroupMemberRole;
import matchuri.backend.domain.group.entity.GroupRecommendation;
import matchuri.backend.domain.group.entity.GroupRecommendationCandidate;
import matchuri.backend.domain.group.entity.GroupRecommendationStatus;
import matchuri.backend.domain.group.entity.GroupRoom;
import matchuri.backend.domain.group.entity.GroupRoomMember;
import matchuri.backend.domain.image.entity.ImageAsset;
import matchuri.backend.domain.image.entity.ImageStorageProvider;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberLocation;
import matchuri.backend.domain.member.entity.MemberProfileImage;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.entity.MemberTasteProfileCategory;
import matchuri.backend.domain.member.entity.MemberTasteProfileDislikedMenuItem;
import matchuri.backend.domain.member.entity.MemberTasteProfileRestrictionIngredient;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementVersions;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.entity.MenuAttributeCategory;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationCandidate;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;
import matchuri.backend.global.config.MatchuriProperties;
import matchuri.backend.testsupport.JpaAuditTimeFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(JpaAuditTimeFixture.class)
class HomeIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EntityManager entityManager;
    @Autowired private MatchuriProperties matchuriProperties;
    @Autowired private JpaAuditTimeFixture jpaAuditTimeFixture;

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void measureHomeQueryAfterOptimization(CapturedOutput output) throws Exception {
        Member member = member(true, MemberStatus.ACTIVE);
        ImageAsset image = persist(new ImageAsset(
                ImageStorageProvider.CLOUDFLARE_R2,
                "test",
                "home/query-profile.png",
                "query-profile.png",
                "image/png",
                100L,
                "b".repeat(64),
                300,
                300
        ));
        persist(new MemberProfileImage(member, image));
        persist(new MemberLocation(
                member,
                new BigDecimal("37.49"),
                new BigDecimal("127.02"),
                1000,
                "서울 서초구"
        ));
        AttributeCategory category = persist(
                new AttributeCategory(CategoryType.FLAVOR, key(), "매콤", 10));
        MemberTasteProfile profile = persist(new MemberTasteProfile(member, "v1"));
        persist(new MemberTasteProfileCategory(profile, category));
        MenuItem menu = persist(new MenuItem(key(), "김치찌개", null));
        persist(new MenuAttributeCategory(menu, category));
        personal(member, menu, LocalDateTime.now().minusDays(1));
        personal(member, null, LocalDateTime.now().minusMinutes(1));
        group(room(member), LocalDateTime.now(), null);

        JsonNode data = home(member);

        assertThat(data.path("user").path("profileImageUrl").asText()).endsWith("home/query-profile.png");
        assertThat(data.path("tasteProfile").path("attributeCategories")).hasSize(1);
        assertThat(data.path("personalRecommendationHistory").path("items")).hasSize(1);
        assertThat(data.path("recentGroupActivities").path("items")).hasSize(1);

        List<String> queryLogs = output.getOut().lines()
                .filter(line -> line.contains(
                        "API_QUERY_BEFORE method=GET uri=/api/v1/home status=200"))
                .toList();

        assertThat(queryLogs)
                .singleElement()
                .satisfies(line -> assertThat(line).contains(
                        "total=7 select=7 insert=0 update=0 delete=0 other=0"));
    }

    @Test
    void homeReturnsComponentDataWithThreeLatestSelectedRecommendationsAndCurrentMenuMetadata() throws Exception {
        Member member = member(true, MemberStatus.ACTIVE);
        Member other = member(true, MemberStatus.ACTIVE);
        var image = persist(new ImageAsset(ImageStorageProvider.CLOUDFLARE_R2, "test", "home/profile.png", "profile.png",
                "image/png", 100L, "a".repeat(64), 300, 300));
        persist(new MemberProfileImage(member, image));
        persist(new MemberLocation(member, new BigDecimal("37.49"), new BigDecimal("127.02"), 1000, "서울 서초구"));
        var spicy = persist(new AttributeCategory(CategoryType.FLAVOR, key(), "매콤", 10));
        var korean = persist(new AttributeCategory(CategoryType.FOOD_CATEGORY, key(), "한식", 10));
        var inactive = persist(new AttributeCategory(CategoryType.TEXTURE, key(), "이전 속성", 10));
        inactive.deactivate();
        var menu = persist(new MenuItem(key(), "이전 메뉴명", null));
        persist(new MenuAttributeCategory(menu, korean));
        persist(new MenuAttributeCategory(menu, spicy));
        persist(new MenuAttributeCategory(menu, inactive));
        var profile = persist(new MemberTasteProfile(member, "v1"));
        persist(new MemberTasteProfileCategory(profile, korean));
        persist(new MemberTasteProfileCategory(profile, spicy));
        for (int i = 0; i < 4; i++) {
            var extra = persist(new AttributeCategory(CategoryType.FLAVOR, key(), "추가 취향 " + i, 20 + i));
            persist(new MemberTasteProfileCategory(profile, extra));
        }
        var restriction = persist(new Ingredient(key(), "제한 재료", true, 10));
        persist(new MemberTasteProfileRestrictionIngredient(profile, restriction));
        persist(new MemberTasteProfileDislikedMenuItem(profile, menu));

        LocalDateTime now = LocalDateTime.now().withNano(0);
        personal(member, menu, now.minusDays(5));
        personal(member, menu, now.minusDays(4));
        var third = personal(member, menu, now.minusDays(3));
        var second = personal(member, menu, now.minusDays(2));
        var first = personal(member, menu, now.minusDays(2)); // same timestamp: ID breaks ties
        var latest = personal(member, null, now.minusMinutes(1));
        personal(other, menu, now); // must never leak into this member's history
        menu.updateName("최신 김치찌개");
        menu.deactivate(); // historical selections survive catalogue deactivation

        JsonNode data = home(member);
        assertThat(data.path("user").path("nickname").asText()).isEqualTo(member.getNickname());
        assertThat(data.path("user").path("profileImageUrl").asText()).endsWith("home/profile.png");
        assertThat(data.path("user").has("email")).isFalse();
        assertThat(data.path("location").path("longitude").decimalValue()).isEqualByComparingTo("127.02");
        assertThat(data.path("location").path("latitude").decimalValue()).isEqualByComparingTo("37.49");
        assertThat(data.path("location").path("address").asText()).isEqualTo("서울 서초구");
        assertThat(data.path("tasteProfile").path("attributeCategories")).hasSize(6);
        assertThat(data.path("tasteProfile").has("restrictionIngredients")).isFalse();
        assertThat(data.path("tasteProfile").has("dislikedMenuItems")).isFalse();
        assertThat(data.path("personalRecommendation").path("latestRecommendationId").asLong()).isEqualTo(latest.getId());
        assertThat(data.path("personalRecommendation").path("latestRecommendationStatus").asText()).isEqualTo("OPEN");
        var items = data.path("personalRecommendationHistory").path("items");
        assertThat(items).hasSize(3);
        assertThat(List.of(items.get(0).path("id").asLong(), items.get(1).path("id").asLong(), items.get(2).path("id").asLong()))
                .containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(LocalDateTime.parse(items.get(0).path("createdAt").asText())).isEqualTo(first.getRequestedAt());
        var selectedMenu = items.get(0).path("selectedMenu");
        assertThat(selectedMenu.path("name").asText()).isEqualTo("최신 김치찌개");
        assertThat(selectedMenu.path("attributeCategories")).hasSize(2);
        assertThat(selectedMenu.path("attributeCategories").get(0).path("name").asText()).isEqualTo("매콤");
        assertThat(selectedMenu.path("attributeCategories").get(1).path("name").asText()).isEqualTo("한식");
        assertThat(selectedMenu.has("thumbnailUrl")).isFalse();
        assertThat(data.path("personalRecommendationHistory").has("pageInfo")).isFalse();
    }

    @Test
    void memberWithoutOptionalDataReceivesStableEmptySections() throws Exception {
        JsonNode data = home(member(true, MemberStatus.ACTIVE));
        assertThat(data.path("user").path("profileImageUrl").isNull()).isTrue();
        assertThat(data.path("location").isNull()).isTrue();
        assertThat(data.path("personalRecommendation").path("latestRecommendationId").isNull()).isTrue();
        assertThat(data.path("personalRecommendation").path("latestRecommendationStatus").isNull()).isTrue();
        assertThat(data.path("tasteProfile").path("attributeCategories")).isEmpty();
        assertThat(data.path("personalRecommendationHistory").path("items")).isEmpty();
        assertThat(data.path("recentGroupActivities").path("items")).isEmpty();
    }

    @Test
    void groupActivitiesIncludeRecommendationHistoryAcrossAllActiveMemberships() throws Exception {
        Member member = member(true, MemberStatus.ACTIVE);
        Member other = member(true, MemberStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now().withNano(0);
        var menu = persist(new MenuItem(key(), "이전 메뉴명", null));
        var firstRoom = room(member);
        var previous = group(firstRoom, now.minusMinutes(10), null);
        var latest = group(firstRoom, now.minusMinutes(10), null);
        var secondRoom = room(other);
        persist(new GroupRoomMember(secondRoom, member, GroupMemberRole.MEMBER, now));
        group(secondRoom, now.minusMinutes(20), menu);
        secondRoom.close(); // ACTIVE membership in a non-deleted room remains visible
        menu.updateName("최신 마라탕");
        room(member); // no recommendation: no activity
        var deleted = room(member);
        group(deleted, now, null);
        deleted.delete();
        var left = room(other);
        var leftMembership = persist(new GroupRoomMember(left, member, GroupMemberRole.MEMBER, now));
        leftMembership.leave(now);
        group(left, now, null);
        var kicked = room(other);
        var kickedMembership = persist(new GroupRoomMember(kicked, member, GroupMemberRole.MEMBER, now));
        kickedMembership.kick(now);
        group(kicked, now, null);
        group(room(other), now, null);
        for (int i = 0; i < 21; i++) {
            group(room(member), now.minusHours(1).minusMinutes(i), null);
        }
        JsonNode items = home(member).path("recentGroupActivities").path("items");
        assertThat(items).hasSize(24); // same group history is retained without default truncation
        assertThat(items.get(0).path("groupId").asLong()).isEqualTo(firstRoom.getId());
        assertThat(items.get(0).path("groupName").asText()).isEqualTo(firstRoom.getName());
        assertThat(items.get(0).path("type").asText()).isEqualTo("OPEN");
        assertThat(items.get(0).path("details").path("recommendationId").asLong()).isEqualTo(latest.getId());
        assertThat(items.get(0).path("details").path("createdAt").isNull()).isFalse();
        assertThat(items.get(0).path("details").path("startedAt").isNull()).isFalse();
        assertThat(items.get(0).path("details").path("endedAt").isNull()).isTrue();
        assertThat(items.get(0).path("details").path("selectedMenuName").isNull()).isTrue();
        assertThat(items.get(1).path("groupId").asLong()).isEqualTo(firstRoom.getId());
        assertThat(items.get(1).path("details").path("recommendationId").asLong()).isEqualTo(previous.getId());
        assertThat(items.get(2).path("groupId").asLong()).isEqualTo(secondRoom.getId());
        assertThat(items.get(2).path("type").asText()).isEqualTo("FINALIZED");
        assertThat(items.get(2).path("details").path("selectedMenuName").asText()).isEqualTo("최신 마라탕");
    }

    @Test
    void groupActivitiesExposeCreatedVotingStartedAndEndedAtBySessionState() throws Exception {
        Member member = member(true, MemberStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now().withNano(0);
        var preparing = preparing(room(member), now.minusMinutes(30));
        var open = preparing(room(member), now.minusMinutes(20));
        open.open(now.minusMinutes(15));
        var menu = persist(new MenuItem(key(), "김치찌개", null));
        var finalized = preparing(room(member), now.minusMinutes(10));
        finalized.open(now.minusMinutes(5));
        var candidate = persist(new GroupRecommendationCandidate(finalized, menu, 1, 90.0, null));
        finalized.finalizeWith(candidate, now);

        JsonNode items = home(member).path("recentGroupActivities").path("items");
        JsonNode preparingItem = findActivity(items, preparing.getId());
        JsonNode openItem = findActivity(items, open.getId());
        JsonNode finalizedItem = findActivity(items, finalized.getId());

        assertThat(LocalDateTime.parse(preparingItem.path("details").path("createdAt").asText()))
                .isEqualTo(now.minusMinutes(30));
        assertThat(preparingItem.path("details").path("startedAt").isNull()).isTrue();
        assertThat(preparingItem.path("details").path("endedAt").isNull()).isTrue();
        assertThat(LocalDateTime.parse(openItem.path("details").path("startedAt").asText()))
                .isEqualTo(now.minusMinutes(15));
        assertThat(openItem.path("details").path("endedAt").isNull()).isTrue();
        assertThat(LocalDateTime.parse(finalizedItem.path("details").path("endedAt").asText())).isEqualTo(now);
        assertThat(items.get(0).path("details").path("recommendationId").asLong()).isEqualTo(finalized.getId());
    }

    @Test
    void homePersistsLazyExpirationForLatestPersonalAndGroupRecommendations() throws Exception {
        Member member = member(true, MemberStatus.ACTIVE);
        LocalDateTime old = LocalDateTime.now().minusDays(2);
        var personal = personal(member, null, old);
        var preparing = preparing(room(member), old);
        var open = group(room(member), old, LocalDateTime.now(), null);
        JsonNode data = home(member);
        assertThat(data.path("personalRecommendation").path("latestRecommendationStatus").asText()).isEqualTo("EXPIRED");
        for (JsonNode activity : data.path("recentGroupActivities").path("items")) {
            assertThat(activity.path("type").asText()).isEqualTo("EXPIRED");
            assertThat(activity.path("details").path("endedAt").isNull()).isFalse();
        }
        entityManager.flush();
        entityManager.clear();
        assertThat(entityManager.find(PersonalRecommendation.class, personal.getId()).getStatus()).isEqualTo(PersonalRecommendationStatus.EXPIRED);
        assertThat(entityManager.find(GroupRecommendation.class, preparing.getId()).getStatus()).isEqualTo(GroupRecommendationStatus.EXPIRED);
        assertThat(entityManager.find(GroupRecommendation.class, open.getId()).getStatus()).isEqualTo(GroupRecommendationStatus.EXPIRED);
    }

    @Test
    void homeRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/home"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("AUTH_TOKEN_MISSING"));
        mockMvc.perform(get("/api/v1/home").header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_TOKEN_INVALID"));
    }

    @Test
    void homeRejectsMissingRequiredAgreements() throws Exception {
        Member member = member(true, MemberStatus.ACTIVE);
        entityManager.flush();
        mockMvc.perform(get("/api/v1/home").header(HttpHeaders.AUTHORIZATION, token(member, false)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("MEMBER_AGREEMENT_REQUIRED"));
    }

    @Test
    void homeRejectsIncompleteNickname() throws Exception {
        Member member = member(false, MemberStatus.ACTIVE);
        entityManager.flush();
        mockMvc.perform(get("/api/v1/home").header(HttpHeaders.AUTHORIZATION, token(member, true)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("MEMBER_NICKNAME_REQUIRED"));
    }

    @Test
    void homeRejectsInactiveMembers() throws Exception {
        Member member = member(true, MemberStatus.INACTIVE);
        entityManager.flush();
        mockMvc.perform(get("/api/v1/home").header(HttpHeaders.AUTHORIZATION, token(member, true)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("MEMBER_INACTIVE_MEMBER"));
    }

    private JsonNode home(Member member) throws Exception {
        entityManager.flush();
        entityManager.clear();
        var response = mockMvc.perform(get("/api/v1/home").header(HttpHeaders.AUTHORIZATION, token(member, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse();
        return objectMapper.readTree(response.getContentAsString(StandardCharsets.UTF_8)).path("data");
    }

    private Member member(boolean nicknameCompleted, MemberStatus status) {
        String name = key();
        return persist(Member.builder().loginId(name).passwordHash("encoded-password")
                .nickname(name).email(name + "@example.com").nicknameCompleted(nicknameCompleted)
                .social(false).memberRole(MemberRole.MEMBER).status(status).build());
    }

    private PersonalRecommendation personal(Member member, MenuItem menu, LocalDateTime requestedAt) {
        var recommendation = PersonalRecommendation.of(member);
        ReflectionTestUtils.setField(recommendation, "requestedAt", requestedAt);
        persist(recommendation);
        if (menu != null) {
            var candidate = persist(PersonalRecommendationCandidate.of(recommendation, menu, 1, 90.0));
            recommendation.select(candidate, requestedAt.plusMinutes(5));
        }
        return recommendation;
    }

    private GroupRoom room(Member owner) {
        return persist(GroupRoom.createOwnedBy(key(), key(), owner));
    }

    private GroupRecommendation group(GroupRoom room, LocalDateTime startedAt, MenuItem selectedMenu) {
        return group(room, startedAt.minusMinutes(5), startedAt, selectedMenu);
    }

    private GroupRecommendation group(
            GroupRoom room,
            LocalDateTime createdAt,
            LocalDateTime startedAt,
            MenuItem selectedMenu
    ) {
        var recommendation = jpaAuditTimeFixture.persistGroupRecommendationAt(
                new GroupRecommendation(room, startedAt),
                createdAt
        );
        if (selectedMenu != null) {
            var candidate = persist(new GroupRecommendationCandidate(recommendation, selectedMenu, 1, 90.0, null));
            recommendation.finalizeWith(candidate, startedAt.plusMinutes(5));
        }
        return recommendation;
    }

    private JsonNode findActivity(JsonNode items, Long recommendationId) {
        for (JsonNode item : items) {
            if (item.path("details").path("recommendationId").asLong() == recommendationId) {
                return item;
            }
        }
        throw new AssertionError("Home activity not found: " + recommendationId);
    }

    private GroupRecommendation preparing(GroupRoom room, LocalDateTime createdAt) {
        return jpaAuditTimeFixture.persistGroupRecommendationAt(
                GroupRecommendation.preparing(room),
                createdAt
        );
    }

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    private String key() {
        return "home-" + UUID.randomUUID().toString().substring(0, 16);
    }

    private String token(Member member, boolean agreed) {
        Instant now = Instant.now();
        var builder = Jwts.builder().issuer(matchuriProperties.getAuth().getJwt().getIssuer())
                .subject(String.valueOf(member.getId())).claim("role", member.getMemberRole().name())
                .claim("loginId", member.getLoginId());
        if (agreed) {
            builder.claim("requiredAgreementRevision", RequiredAgreementVersions.currentRevision());
        }
        return "Bearer " + builder.issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(matchuriProperties.getAuth().getJwt().getSecret().getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
