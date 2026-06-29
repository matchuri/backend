package matchuri.backend.api.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import matchuri.backend.domain.behavior.entity.ActionType;
import matchuri.backend.domain.behavior.repository.MemberMenuActionRepository;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.entity.MemberTasteProfileCategory;
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
import matchuri.backend.domain.recommendation.entity.PersonalRecommendation;
import matchuri.backend.domain.recommendation.entity.PersonalRecommendationStatus;
import matchuri.backend.domain.recommendation.repository.PersonalRecommendationCandidateRepository;
import matchuri.backend.domain.recommendation.repository.PersonalRecommendationRepository;
import matchuri.backend.global.config.MatchuriProperties;
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
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PersonalRecommendationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MatchuriProperties matchuriProperties;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AttributeCategoryRepository attributeCategoryRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

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

    @Autowired
    private PersonalRecommendationRepository personalRecommendationRepository;

    @Autowired
    private PersonalRecommendationCandidateRepository personalRecommendationCandidateRepository;

    @Autowired
    private MemberMenuActionRepository memberMenuActionRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("update personal_recommendations set selected_candidate_id = null");
        memberMenuActionRepository.deleteAll();
        personalRecommendationCandidateRepository.deleteAll();
        personalRecommendationRepository.deleteAll();
        memberTasteProfileCategoryRepository.deleteAll();
        memberTasteProfileRestrictionIngredientRepository.deleteAll();
        memberTasteProfileDislikedMenuItemRepository.deleteAll();
        memberTasteProfileRepository.deleteAll();
        menuIngredientRepository.deleteAll();
        menuAttributeCategoryRepository.deleteAll();
        menuItemRepository.deleteAll();
        attributeCategoryRepository.deleteAll();
        ingredientRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("개인 추천 생성, 상세 조회, 후보 선택은 CHOOSE 로그까지 저장한다")
    void personalRecommendationFlow() throws Exception {
        Member member = saveMember("lunch-user", "점심탐험가");
        String accessToken = accessToken(member);
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        Ingredient peanut = ingredientRepository.save(new Ingredient("PEANUT", "땅콩", true, 10));
        MenuItem bibimbap = menuItemRepository.save(new MenuItem("BIBIMBAP", "비빔밥", "채소와 밥"));
        MenuItem peanutNoodle = menuItemRepository.save(new MenuItem("PEANUT_NOODLE", "땅콩면", "땅콩 소스 면"));
        MenuItem riceNoodle = menuItemRepository.save(new MenuItem("RICE_NOODLE", "쌀국수", "가벼운 국물 메뉴"));
        saveMenuAttribute(bibimbap, spicy);
        saveMenuAttribute(peanutNoodle, spicy);
        menuIngredientRepository.save(new MenuIngredient(peanutNoodle, peanut));
        saveTasteProfile(member, spicy, peanut);

        MvcResult createResult = mockMvc.perform(post("/api/v1/personal/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contextJson": {
                                    "mealTime": "LUNCH",
                                    "budgetLevel": 2
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.status").value(PersonalRecommendationStatus.OPEN.name()))
                .andExpect(jsonPath("$.data.closedAt").value(nullValue()))
                .andExpect(jsonPath("$.data.candidates.length()").value(2))
                .andExpect(jsonPath("$.data.candidates[0].menuId").value(bibimbap.getId()))
                .andExpect(jsonPath("$.data.candidates[0].rankNo").value(1))
                .andExpect(jsonPath("$.data.candidates[1].menuId").value(riceNoodle.getId()))
                .andReturn();

        JsonNode createData = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data");
        long requestId = createData.path("requestId").asLong();
        long firstCandidateId = createData.path("candidates").get(0).path("id").asLong();

        assertThat(personalRecommendationRepository.count()).isEqualTo(1);
        assertThat(personalRecommendationCandidateRepository.count()).isEqualTo(2);

        mockMvc.perform(get("/api/v1/personal/recommendations/{requestId}", requestId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(requestId))
                .andExpect(jsonPath("$.data.contextJson.mealTime").value("LUNCH"))
                .andExpect(jsonPath("$.data.closedAt").value(nullValue()))
                .andExpect(jsonPath("$.data.candidates.length()").value(2))
                .andExpect(jsonPath("$.data.selectedCandidateId").value(nullValue()));

        mockMvc.perform(get("/api/v1/personal/recommendations/{requestId}/candidates", requestId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestId").value(requestId))
                .andExpect(jsonPath("$.data.candidates[0].id").value(firstCandidateId));

        mockMvc.perform(get("/api/v1/personal/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(requestId))
                .andExpect(jsonPath("$.data.content[0].closedAt").value(nullValue()))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1));

        mockMvc.perform(patch("/api/v1/personal/recommendations/{requestId}", requestId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "selectedCandidateId": %d
                                }
                                """.formatted(firstCandidateId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(requestId))
                .andExpect(jsonPath("$.data.status").value(PersonalRecommendationStatus.SELECTED.name()))
                .andExpect(jsonPath("$.data.selectedCandidateId").value(firstCandidateId))
                .andExpect(jsonPath("$.data.closedAt").exists());

        assertThat(memberMenuActionRepository.count()).isEqualTo(1);
        assertThat(memberMenuActionRepository.findAll().getFirst().getActionType()).isEqualTo(ActionType.CHOOSE);

        PersonalRecommendation selectedRecommendation = personalRecommendationRepository.findById(requestId)
                .orElseThrow();
        assertThat(selectedRecommendation.getStatus()).isEqualTo(PersonalRecommendationStatus.SELECTED);
        assertThat(selectedRecommendation.getClosedAt()).isNotNull();

        mockMvc.perform(get("/api/v1/personal/recommendations/{requestId}", requestId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(PersonalRecommendationStatus.SELECTED.name()))
                .andExpect(jsonPath("$.data.closedAt").exists());
    }

    @Test
    @DisplayName("개인 추천 생성은 취향 프로필이 없으면 실패한다")
    void createPersonalRecommendationFailsWithoutTasteProfile() throws Exception {
        Member member = saveMember("no-profile-user", "프로필없음");

        mockMvc.perform(post("/api/v1/personal/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contextJson": {}
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("PERSONAL_RECOMMENDATION_TASTE_PROFILE_REQUIRED"));
    }

    @Test
    @DisplayName("개인 추천은 제외 조건 이후 후보가 없으면 빈 후보 목록을 저장하고 반환한다")
    void createPersonalRecommendationReturnsEmptyCandidatesWhenAllMenusAreExcluded() throws Exception {
        Member member = saveMember("empty-candidate-user", "후보없음");
        String accessToken = accessToken(member);
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        Ingredient peanut = ingredientRepository.save(new Ingredient("PEANUT", "땅콩", true, 10));
        MenuItem peanutNoodle = menuItemRepository.save(new MenuItem("PEANUT_NOODLE", "땅콩면", "땅콩 소스 면"));
        saveMenuAttribute(peanutNoodle, spicy);
        menuIngredientRepository.save(new MenuIngredient(peanutNoodle, peanut));
        saveTasteProfile(member, spicy, peanut);

        mockMvc.perform(post("/api/v1/personal/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contextJson": {}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(PersonalRecommendationStatus.OPEN.name()))
                .andExpect(jsonPath("$.data.candidates.length()").value(0));

        assertThat(personalRecommendationRepository.count()).isEqualTo(1);
        assertThat(personalRecommendationCandidateRepository.count()).isZero();
    }

    @Test
    @DisplayName("개인 추천 생성은 24시간 이내 열린 추천이 있으면 거절한다")
    void createPersonalRecommendationRejectsWhenOpenRecommendationExists() throws Exception {
        Member member = saveMember("open-exists-user", "열린추천");
        String accessToken = accessToken(member);
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        MenuItem bibimbap = menuItemRepository.save(new MenuItem("BIBIMBAP", "비빔밥", "채소와 밥"));
        saveMenuAttribute(bibimbap, spicy);
        saveTasteProfile(member, spicy, null);

        JsonNode first = createRecommendation(accessToken);
        long firstRequestId = first.path("requestId").asLong();

        mockMvc.perform(post("/api/v1/personal/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contextJson": {}
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("PERSONAL_RECOMMENDATION_OPEN_EXISTS"));

        assertThat(personalRecommendationRepository.count()).isEqualTo(1);
        PersonalRecommendation openRecommendation = personalRecommendationRepository.findById(firstRequestId)
                .orElseThrow();
        assertThat(openRecommendation.getStatus()).isEqualTo(PersonalRecommendationStatus.OPEN);
        assertThat(openRecommendation.getClosedAt()).isNull();
    }

    @Test
    @DisplayName("개인 추천 생성은 시간상 만료된 열린 추천을 무시하고 새 추천을 생성한다")
    void createPersonalRecommendationIgnoresExpiredOpenRecommendationAndCreatesNewOne() throws Exception {
        Member member = saveMember("expired-open-user", "만료추천");
        String accessToken = accessToken(member);
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        MenuItem bibimbap = menuItemRepository.save(new MenuItem("BIBIMBAP", "비빔밥", "채소와 밥"));
        saveMenuAttribute(bibimbap, spicy);
        saveTasteProfile(member, spicy, null);

        JsonNode first = createRecommendation(accessToken);
        long firstRequestId = first.path("requestId").asLong();
        jdbcTemplate.update(
                "update personal_recommendations set requested_at = ? where id = ?",
                LocalDateTime.now().minusHours(25),
                firstRequestId
        );

        JsonNode second = createRecommendation(accessToken);
        long secondRequestId = second.path("requestId").asLong();

        assertThat(secondRequestId).isNotEqualTo(firstRequestId);
        assertThat(personalRecommendationRepository.count()).isEqualTo(2);

        PersonalRecommendation oldRecommendation = personalRecommendationRepository.findById(firstRequestId)
                .orElseThrow();
        PersonalRecommendation newRecommendation = personalRecommendationRepository.findById(secondRequestId)
                .orElseThrow();
        assertThat(oldRecommendation.getStatus()).isEqualTo(PersonalRecommendationStatus.EXPIRED);
        assertThat(oldRecommendation.getClosedAt()).isNotNull();
        assertThat(newRecommendation.getStatus()).isEqualTo(PersonalRecommendationStatus.OPEN);
        assertThat(newRecommendation.getClosedAt()).isNull();
    }

    @Test
    @DisplayName("개인 추천 목록은 시간상 만료된 열린 추천을 EXPIRED로 전환해 반환한다")
    void getMyPersonalRecommendationsExpiresOpenRecommendation() throws Exception {
        Member member = saveMember("expiration-list-user", "만료목록");
        String accessToken = accessToken(member);
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        MenuItem bibimbap = menuItemRepository.save(new MenuItem("BIBIMBAP", "비빔밥", "채소와 밥"));
        saveMenuAttribute(bibimbap, spicy);
        saveTasteProfile(member, spicy, null);

        JsonNode recommendation = createRecommendation(accessToken);
        long requestId = recommendation.path("requestId").asLong();
        expireRequestedAt(requestId);

        mockMvc.perform(get("/api/v1/personal/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(requestId))
                .andExpect(jsonPath("$.data.content[0].status").value(PersonalRecommendationStatus.EXPIRED.name()))
                .andExpect(jsonPath("$.data.content[0].closedAt").isNotEmpty());

        PersonalRecommendation oldRecommendation = personalRecommendationRepository.findById(requestId)
                .orElseThrow();
        assertThat(oldRecommendation.getStatus()).isEqualTo(PersonalRecommendationStatus.EXPIRED);
        assertThat(oldRecommendation.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("개인 추천 상세 조회는 시간상 만료된 열린 추천을 EXPIRED로 전환해 반환한다")
    void getPersonalRecommendationExpiresOpenRecommendation() throws Exception {
        Member member = saveMember("expiration-detail-user", "만료상세");
        String accessToken = accessToken(member);
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        MenuItem bibimbap = menuItemRepository.save(new MenuItem("BIBIMBAP", "비빔밥", "채소와 밥"));
        saveMenuAttribute(bibimbap, spicy);
        saveTasteProfile(member, spicy, null);

        JsonNode recommendation = createRecommendation(accessToken);
        long requestId = recommendation.path("requestId").asLong();
        expireRequestedAt(requestId);

        mockMvc.perform(get("/api/v1/personal/recommendations/{requestId}", requestId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(requestId))
                .andExpect(jsonPath("$.data.status").value(PersonalRecommendationStatus.EXPIRED.name()))
                .andExpect(jsonPath("$.data.closedAt").isNotEmpty());

        PersonalRecommendation oldRecommendation = personalRecommendationRepository.findById(requestId)
                .orElseThrow();
        assertThat(oldRecommendation.getStatus()).isEqualTo(PersonalRecommendationStatus.EXPIRED);
        assertThat(oldRecommendation.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("24시간이 지난 개인 추천은 선택할 수 없다")
    void selectPersonalRecommendationRejectsExpiredByTimeRecommendation() throws Exception {
        Member member = saveMember("select-expired-user", "선택만료");
        String accessToken = accessToken(member);
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        MenuItem bibimbap = menuItemRepository.save(new MenuItem("BIBIMBAP", "비빔밥", "채소와 밥"));
        saveMenuAttribute(bibimbap, spicy);
        saveTasteProfile(member, spicy, null);

        JsonNode recommendation = createRecommendation(accessToken);
        long requestId = recommendation.path("requestId").asLong();
        long candidateId = recommendation.path("candidates").get(0).path("id").asLong();
        expireRequestedAt(requestId);

        mockMvc.perform(patch("/api/v1/personal/recommendations/{requestId}", requestId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "selectedCandidateId": %d
                                }
                """.formatted(candidateId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("PERSONAL_RECOMMENDATION_EXPIRED"));

        PersonalRecommendation oldRecommendation = personalRecommendationRepository.findById(requestId)
                .orElseThrow();
        assertThat(oldRecommendation.getSelectedCandidate()).isNull();
        assertThat(oldRecommendation.getStatus()).isEqualTo(PersonalRecommendationStatus.EXPIRED);
        assertThat(oldRecommendation.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("24시간이 지난 개인 추천은 재요청할 수 없다")
    void rerollPersonalRecommendationRejectsExpiredByTimeRecommendation() throws Exception {
        Member member = saveMember("reroll-expired-user", "재요청만료");
        String accessToken = accessToken(member);
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        MenuItem bibimbap = menuItemRepository.save(new MenuItem("BIBIMBAP", "비빔밥", "채소와 밥"));
        saveMenuAttribute(bibimbap, spicy);
        saveTasteProfile(member, spicy, null);

        JsonNode recommendation = createRecommendation(accessToken);
        long requestId = recommendation.path("requestId").asLong();
        expireRequestedAt(requestId);

        mockMvc.perform(post("/api/v1/personal/recommendations/{requestId}/reroll", requestId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rerollType": "INPUT_CHANGED",
                                  "contextJson": {}
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("PERSONAL_RECOMMENDATION_EXPIRED"));

        PersonalRecommendation oldRecommendation = personalRecommendationRepository.findById(requestId)
                .orElseThrow();
        assertThat(oldRecommendation.getStatus()).isEqualTo(PersonalRecommendationStatus.EXPIRED);
        assertThat(oldRecommendation.getClosedAt()).isNotNull();
        assertThat(personalRecommendationRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("불만족 개인 추천 재요청은 이전 후보를 SKIP 로그로 저장하고 새 추천을 생성한다")
    void rerollPersonalRecommendationWithNotSatisfiedClosesWithSkipAndCreatesNewRecommendation() throws Exception {
        Member member = saveMember("reroll-skip-user", "불만족재요청");
        String accessToken = accessToken(member);
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        MenuItem bibimbap = menuItemRepository.save(new MenuItem("BIBIMBAP", "비빔밥", "채소와 밥"));
        MenuItem riceNoodle = menuItemRepository.save(new MenuItem("RICE_NOODLE", "쌀국수", "가벼운 국물 메뉴"));
        saveMenuAttribute(bibimbap, spicy);
        saveMenuAttribute(riceNoodle, spicy);
        saveTasteProfile(member, spicy, null);

        JsonNode source = createRecommendation(accessToken);
        long sourceRequestId = source.path("requestId").asLong();
        List<Long> sourceCandidateMenuIds = candidateMenuIds(source);
        int sourceCandidateCount = source.path("candidates").size();

        MvcResult rerollResult = mockMvc.perform(post(
                                "/api/v1/personal/recommendations/{requestId}/reroll",
                                sourceRequestId
                        )
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rerollType": "NOT_SATISFIED",
                                  "contextJson": {
                                    "mealTime": "LUNCH",
                                    "mood": "다른 메뉴"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestId").isNumber())
                .andExpect(jsonPath("$.data.closedAt").value(nullValue()))
                .andExpect(jsonPath("$.data.status").value(PersonalRecommendationStatus.OPEN.name()))
                .andReturn();

        long rerolledRequestId = objectMapper.readTree(rerollResult.getResponse().getContentAsString())
                .path("data")
                .path("requestId")
                .asLong();
        JsonNode rerolledData = objectMapper.readTree(rerollResult.getResponse().getContentAsString()).path("data");

        assertThat(rerolledRequestId).isNotEqualTo(sourceRequestId);
        assertThat(candidateMenuIds(rerolledData)).doesNotContainAnyElementsOf(sourceCandidateMenuIds);
        assertThat(personalRecommendationRepository.count()).isEqualTo(2);
        assertThat(memberMenuActionRepository.findAll())
                .hasSize(sourceCandidateCount)
                .allSatisfy(action -> assertThat(action.getActionType()).isEqualTo(ActionType.SKIP));

        PersonalRecommendation sourceRecommendation = personalRecommendationRepository.findById(sourceRequestId)
                .orElseThrow();
        assertThat(sourceRecommendation.getStatus()).isEqualTo(PersonalRecommendationStatus.REROLLED_WITH_SKIP);
        assertThat(sourceRecommendation.getClosedAt()).isNotNull();

        jdbcTemplate.update(
                "update member_menu_actions set created_at = ? where personal_recommendation_id = ?",
                LocalDateTime.now().minusHours(25),
                sourceRequestId
        );

        MvcResult afterSkipWindowResult = mockMvc.perform(post(
                                "/api/v1/personal/recommendations/{requestId}/reroll",
                                rerolledRequestId
                        )
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rerollType": "INPUT_CHANGED",
                                  "contextJson": {
                                    "mealTime": "LUNCH",
                                    "mood": "다시 후보 허용"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode afterSkipWindowData = objectMapper.readTree(afterSkipWindowResult.getResponse().getContentAsString())
                .path("data");
        assertThat(candidateMenuIds(afterSkipWindowData)).containsAnyElementsOf(sourceCandidateMenuIds);
    }

    @Test
    @DisplayName("입력 변경 개인 추천 재요청은 SKIP 로그 없이 이전 추천을 종료하고 새 추천을 생성한다")
    void rerollPersonalRecommendationWithInputChangedClosesWithoutSkipAndCreatesNewRecommendation() throws Exception {
        Member member = saveMember("reroll-input-user", "입력변경재요청");
        String accessToken = accessToken(member);
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        MenuItem bibimbap = menuItemRepository.save(new MenuItem("BIBIMBAP", "비빔밥", "채소와 밥"));
        saveMenuAttribute(bibimbap, spicy);
        saveTasteProfile(member, spicy, null);

        JsonNode source = createRecommendation(accessToken);
        long sourceRequestId = source.path("requestId").asLong();
        List<Long> sourceCandidateMenuIds = candidateMenuIds(source);

        MvcResult rerollResult = mockMvc.perform(post(
                                "/api/v1/personal/recommendations/{requestId}/reroll",
                                sourceRequestId
                        )
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rerollType": "INPUT_CHANGED",
                                  "contextJson": {
                                    "mealTime": "DINNER"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestId").isNumber())
                .andReturn();

        long rerolledRequestId = objectMapper.readTree(rerollResult.getResponse().getContentAsString())
                .path("data")
                .path("requestId")
                .asLong();
        JsonNode rerolledData = objectMapper.readTree(rerollResult.getResponse().getContentAsString()).path("data");

        assertThat(rerolledRequestId).isNotEqualTo(sourceRequestId);
        assertThat(candidateMenuIds(rerolledData)).containsAnyElementsOf(sourceCandidateMenuIds);
        assertThat(memberMenuActionRepository.count()).isZero();

        PersonalRecommendation sourceRecommendation = personalRecommendationRepository.findById(sourceRequestId)
                .orElseThrow();
        assertThat(sourceRecommendation.getStatus()).isEqualTo(PersonalRecommendationStatus.REROLLED_WITHOUT_SKIP);
        assertThat(sourceRecommendation.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("종료된 개인 추천은 재요청할 수 없다")
    void rerollPersonalRecommendationRejectsClosedRecommendation() throws Exception {
        Member member = saveMember("reroll-closed-user", "종료재요청");
        String accessToken = accessToken(member);
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        MenuItem bibimbap = menuItemRepository.save(new MenuItem("BIBIMBAP", "비빔밥", "채소와 밥"));
        saveMenuAttribute(bibimbap, spicy);
        saveTasteProfile(member, spicy, null);

        JsonNode source = createRecommendation(accessToken);
        long sourceRequestId = source.path("requestId").asLong();

        mockMvc.perform(post("/api/v1/personal/recommendations/{requestId}/reroll", sourceRequestId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rerollType": "INPUT_CHANGED",
                                  "contextJson": {}
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/personal/recommendations/{requestId}/reroll", sourceRequestId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rerollType": "INPUT_CHANGED",
                                  "contextJson": {}
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("PERSONAL_RECOMMENDATION_ALREADY_CLOSED"));
    }

    @Test
    @DisplayName("개인 추천 조회는 타인 추천 접근을 찾을 수 없음으로 거절한다")
    void getPersonalRecommendationRejectsOtherMemberRecommendation() throws Exception {
        Member owner = saveMember("owner-user", "추천주인");
        Member other = saveMember("other-user", "다른회원");
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        MenuItem bibimbap = menuItemRepository.save(new MenuItem("BIBIMBAP", "비빔밥", "채소와 밥"));
        saveMenuAttribute(bibimbap, spicy);
        saveTasteProfile(owner, spicy, null);

        MvcResult createResult = mockMvc.perform(post("/api/v1/personal/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(owner)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contextJson": {}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        long requestId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("requestId")
                .asLong();

        mockMvc.perform(get("/api/v1/personal/recommendations/{requestId}", requestId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(other))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("PERSONAL_RECOMMENDATION_NOT_FOUND"));
    }

    @Test
    @DisplayName("개인 추천 선택은 다른 추천의 후보와 이미 종료된 추천을 거절한다")
    void selectPersonalRecommendationRejectsInvalidCandidateAndAlreadySelectedRecommendation() throws Exception {
        Member member = saveMember("select-user", "선택검증");
        String accessToken = accessToken(member);
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        MenuItem bibimbap = menuItemRepository.save(new MenuItem("BIBIMBAP", "비빔밥", "채소와 밥"));
        saveMenuAttribute(bibimbap, spicy);
        saveTasteProfile(member, spicy, null);

        JsonNode recommendation = createRecommendation(accessToken);
        long requestId = recommendation.path("requestId").asLong();
        long candidateId = recommendation.path("candidates").get(0).path("id").asLong();

        mockMvc.perform(patch("/api/v1/personal/recommendations/{requestId}", requestId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "selectedCandidateId": %d
                                }
                                """.formatted(candidateId + 10_000)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("PERSONAL_RECOMMENDATION_CANDIDATE_NOT_FOUND"));

        mockMvc.perform(patch("/api/v1/personal/recommendations/{requestId}", requestId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "selectedCandidateId": %d
                                }
                                """.formatted(candidateId)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/personal/recommendations/{requestId}", requestId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "selectedCandidateId": %d
                                }
                                """.formatted(candidateId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("PERSONAL_RECOMMENDATION_ALREADY_CLOSED"));
    }

    private JsonNode createRecommendation(String accessToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/personal/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contextJson": {}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private void expireRequestedAt(long requestId) {
        jdbcTemplate.update(
                "update personal_recommendations set requested_at = ? where id = ?",
                LocalDateTime.now().minusHours(25),
                requestId
        );
    }

    private List<Long> candidateMenuIds(JsonNode recommendationData) {
        List<Long> menuIds = new ArrayList<>();
        recommendationData.path("candidates")
                .forEach(candidate -> menuIds.add(candidate.path("menuId").asLong()));

        return menuIds;
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

    private void saveTasteProfile(Member member, AttributeCategory attributeCategory, Ingredient restrictionIngredient) {
        MemberTasteProfile tasteProfile = memberTasteProfileRepository.save(new MemberTasteProfile(member, "v1"));
        memberTasteProfileCategoryRepository.save(new MemberTasteProfileCategory(tasteProfile, attributeCategory));

        if (restrictionIngredient != null) {
            memberTasteProfileRestrictionIngredientRepository.save(
                    new MemberTasteProfileRestrictionIngredient(tasteProfile, restrictionIngredient)
            );
        }
    }

    private void saveMenuAttribute(MenuItem menuItem, AttributeCategory attributeCategory) {
        menuAttributeCategoryRepository.save(new MenuAttributeCategory(menuItem, attributeCategory));
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
