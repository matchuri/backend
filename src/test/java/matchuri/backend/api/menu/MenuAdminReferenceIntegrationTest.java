package matchuri.backend.api.menu;

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
import java.util.Date;
import javax.crypto.SecretKey;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberRole;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.support.agreement.RequiredAgreementVersions;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.global.config.MatchuriProperties;
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
class MenuAdminReferenceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AttributeCategoryRepository attributeCategoryRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MatchuriProperties matchuriProperties;

    @BeforeEach
    void setUp() {
        attributeCategoryRepository.deleteAll();
        ingredientRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("관리자 attribute category 목록 조회는 활성과 비활성 데이터를 함께 정렬해서 반환한다")
    void getAdminAttributeCategoriesReturnsAllRowsInSortedOrder() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        AttributeCategory spicy = attributeCategoryRepository.save(new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 20));
        AttributeCategory grilled = attributeCategoryRepository.save(new AttributeCategory(CategoryType.COOKING_METHOD, "GRILLED", "구이", 10));
        AttributeCategory mild = new AttributeCategory(CategoryType.FLAVOR, "MILD", "순한맛", 10);
        mild.deactivate();
        mild = attributeCategoryRepository.save(mild);

        mockMvc.perform(get("/api/v1/admin/attribute-categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].id").value(grilled.getId()))
                .andExpect(jsonPath("$.data[0].categoryType").value("COOKING_METHOD"))
                .andExpect(jsonPath("$.data[0].isActive").value(true))
                .andExpect(jsonPath("$.data[1].id").value(mild.getId()))
                .andExpect(jsonPath("$.data[1].code").value("MILD"))
                .andExpect(jsonPath("$.data[1].isActive").value(false))
                .andExpect(jsonPath("$.data[2].id").value(spicy.getId()))
                .andExpect(jsonPath("$.data[2].code").value("SPICY"))
                .andExpect(jsonPath("$.data[2].isActive").value(true));
    }

    @Test
    @DisplayName("관리자 ingredient 목록 조회는 활성과 비활성 데이터를 함께 정렬해서 반환한다")
    void getAdminIngredientsReturnsAllRowsInSortedOrder() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-ingredient-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        Ingredient peanut = ingredientRepository.save(new Ingredient("PEANUT", "땅콩", true, 10));
        Ingredient pork = new Ingredient("PORK", "돼지고기", false, 10);
        pork.deactivate();
        pork = ingredientRepository.save(pork);
        Ingredient milk = ingredientRepository.save(new Ingredient("MILK", "우유", true, 30));

        mockMvc.perform(get("/api/v1/admin/ingredients")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].id").value(peanut.getId()))
                .andExpect(jsonPath("$.data[0].code").value("PEANUT"))
                .andExpect(jsonPath("$.data[0].allergen").value(true))
                .andExpect(jsonPath("$.data[0].isActive").value(true))
                .andExpect(jsonPath("$.data[1].id").value(pork.getId()))
                .andExpect(jsonPath("$.data[1].code").value("PORK"))
                .andExpect(jsonPath("$.data[1].allergen").value(false))
                .andExpect(jsonPath("$.data[1].isActive").value(false))
                .andExpect(jsonPath("$.data[2].code").value("MILK"))
                .andExpect(jsonPath("$.data[2].isActive").value(true));
    }

    @Test
    @DisplayName("일반 회원은 관리자 attribute category 목록 조회에 접근할 수 없다")
    void getAdminAttributeCategoriesRejectsNonAdminMember() throws Exception {
        Member member = memberRepository.save(new Member(
                "member-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));

        mockMvc.perform(get("/api/v1/admin/attribute-categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));
    }

    @Test
    @DisplayName("일반 회원은 관리자 ingredient 목록 조회에 접근할 수 없다")
    void getAdminIngredientsRejectsNonAdminMember() throws Exception {
        Member member = memberRepository.save(new Member(
                "member-ingredient-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));

        mockMvc.perform(get("/api/v1/admin/ingredients")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));
    }

    @Test
    @DisplayName("관리자는 ingredient를 생성할 수 있다")
    void createAdminIngredient() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-create-ingredient-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));

        mockMvc.perform(post("/api/v1/admin/ingredients")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "PEANUT",
                                  "name": " 땅콩 ",
                                  "allergen": true,
                                  "sortOrder": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.code").value("PEANUT"))
                .andExpect(jsonPath("$.data.name").value("땅콩"))
                .andExpect(jsonPath("$.data.allergen").value(true))
                .andExpect(jsonPath("$.data.sortOrder").value(10))
                .andExpect(jsonPath("$.data.isActive").value(true));

        assertThat(ingredientRepository.findAll())
                .hasSize(1)
                .first()
                .satisfies(ingredient -> {
                    assertThat(ingredient.getCode()).isEqualTo("PEANUT");
                    assertThat(ingredient.getName()).isEqualTo("땅콩");
                    assertThat(ingredient.isAllergen()).isTrue();
                    assertThat(ingredient.getSortOrder()).isEqualTo(10);
                    assertThat(ingredient.isActive()).isTrue();
                });
    }

    @Test
    @DisplayName("관리자 ingredient 생성은 같은 code 중복을 거절한다")
    void createAdminIngredientRejectsDuplicate() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-duplicate-ingredient-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        ingredientRepository.save(new Ingredient("PEANUT", "땅콩", true, 10));

        mockMvc.perform(post("/api/v1/admin/ingredients")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "PEANUT",
                                  "name": "새 땅콩",
                                  "allergen": false,
                                  "sortOrder": 20
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MENU_INGREDIENT_DUPLICATE"));
    }

    @Test
    @DisplayName("관리자는 ingredient의 수정 가능 필드를 갱신할 수 있다")
    void updateAdminIngredient() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-update-ingredient-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        Ingredient ingredient = ingredientRepository.save(new Ingredient("PEANUT", "땅콩", true, 10));

        mockMvc.perform(patch("/api/v1/admin/ingredients/{ingredientId}", ingredient.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새 땅콩",
                                  "allergen": false,
                                  "sortOrder": 20,
                                  "isActive": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.id").value(ingredient.getId()))
                .andExpect(jsonPath("$.data.code").value("PEANUT"))
                .andExpect(jsonPath("$.data.name").value("새 땅콩"))
                .andExpect(jsonPath("$.data.allergen").value(false))
                .andExpect(jsonPath("$.data.sortOrder").value(20))
                .andExpect(jsonPath("$.data.isActive").value(false));

        Ingredient updated = ingredientRepository.findById(ingredient.getId()).orElseThrow();
        assertThat(updated.getCode()).isEqualTo("PEANUT");
        assertThat(updated.getName()).isEqualTo("새 땅콩");
        assertThat(updated.isAllergen()).isFalse();
        assertThat(updated.getSortOrder()).isEqualTo(20);
        assertThat(updated.isActive()).isFalse();
    }

    @Test
    @DisplayName("관리자 ingredient 수정은 비활성 데이터를 다시 활성화할 수 있다")
    void updateAdminIngredientCanReactivate() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-reactivate-ingredient-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        Ingredient ingredient = new Ingredient("PORK", "돼지고기", false, 10);
        ingredient.deactivate();
        ingredient = ingredientRepository.save(ingredient);

        mockMvc.perform(patch("/api/v1/admin/ingredients/{ingredientId}", ingredient.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isActive": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(ingredient.getId()))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.name").value("돼지고기"))
                .andExpect(jsonPath("$.data.allergen").value(false));
    }

    @Test
    @DisplayName("관리자 ingredient 수정은 존재하지 않는 대상을 거절한다")
    void updateAdminIngredientRejectsNotFound() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-not-found-ingredient-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));

        mockMvc.perform(patch("/api/v1/admin/ingredients/{ingredientId}", 999L)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새 이름"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MENU_INGREDIENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("일반 회원은 관리자 ingredient 수정에 접근할 수 없다")
    void updateAdminIngredientRejectsNonAdminMember() throws Exception {
        Member member = memberRepository.save(new Member(
                "member-patch-ingredient-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        Ingredient ingredient = ingredientRepository.save(new Ingredient("PEANUT", "땅콩", true, 10));

        mockMvc.perform(patch("/api/v1/admin/ingredients/{ingredientId}", ingredient.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새 땅콩"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));
    }

    @Test
    @DisplayName("관리자는 ingredient를 비활성화할 수 있다")
    void deactivateAdminIngredient() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-delete-ingredient-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        Ingredient ingredient = ingredientRepository.save(new Ingredient("PEANUT", "땅콩", true, 10));

        mockMvc.perform(delete("/api/v1/admin/ingredients/{ingredientId}", ingredient.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.id").value(ingredient.getId()))
                .andExpect(jsonPath("$.data.code").value("PEANUT"))
                .andExpect(jsonPath("$.data.isActive").value(false));

        Ingredient deactivated = ingredientRepository.findById(ingredient.getId()).orElseThrow();
        assertThat(deactivated.isActive()).isFalse();
        assertThat(deactivated.getName()).isEqualTo("땅콩");
        assertThat(deactivated.isAllergen()).isTrue();
    }

    @Test
    @DisplayName("관리자 ingredient 비활성화는 이미 비활성 상태여도 현재 상태를 반환한다")
    void deactivateAdminIngredientReturnsCurrentStateWhenAlreadyInactive() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-delete-inactive-ingredient-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        Ingredient ingredient = new Ingredient("PORK", "돼지고기", false, 10);
        ingredient.deactivate();
        ingredient = ingredientRepository.save(ingredient);

        mockMvc.perform(delete("/api/v1/admin/ingredients/{ingredientId}", ingredient.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(ingredient.getId()))
                .andExpect(jsonPath("$.data.isActive").value(false))
                .andExpect(jsonPath("$.data.name").value("돼지고기"));
    }

    @Test
    @DisplayName("관리자 ingredient 비활성화는 존재하지 않는 대상을 거절한다")
    void deactivateAdminIngredientRejectsNotFound() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-delete-not-found-ingredient-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));

        mockMvc.perform(delete("/api/v1/admin/ingredients/{ingredientId}", 999L)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MENU_INGREDIENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("일반 회원은 관리자 ingredient 비활성화에 접근할 수 없다")
    void deactivateAdminIngredientRejectsNonAdminMember() throws Exception {
        Member member = memberRepository.save(new Member(
                "member-delete-ingredient-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        Ingredient ingredient = ingredientRepository.save(new Ingredient("PEANUT", "땅콩", true, 10));

        mockMvc.perform(delete("/api/v1/admin/ingredients/{ingredientId}", ingredient.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));
    }

    @Test
    @DisplayName("관리자는 attribute category를 생성할 수 있다")
    void createAdminAttributeCategory() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-create-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));

        mockMvc.perform(post("/api/v1/admin/attribute-categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryType": "flavor",
                                  "code": "SPICY",
                                  "name": " 매운맛 ",
                                  "sortOrder": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.categoryType").value("FLAVOR"))
                .andExpect(jsonPath("$.data.code").value("SPICY"))
                .andExpect(jsonPath("$.data.name").value("매운맛"))
                .andExpect(jsonPath("$.data.sortOrder").value(10))
                .andExpect(jsonPath("$.data.isActive").value(true));

        assertThat(attributeCategoryRepository.findAll())
                .hasSize(1)
                .first()
                .satisfies(attributeCategory -> {
                    assertThat(attributeCategory.getCategoryType()).isEqualTo(CategoryType.FLAVOR);
                    assertThat(attributeCategory.getCode()).isEqualTo("SPICY");
                    assertThat(attributeCategory.getName()).isEqualTo("매운맛");
                    assertThat(attributeCategory.isActive()).isTrue();
                });
    }

    @Test
    @DisplayName("관리자 attribute category 생성은 같은 categoryType과 code 조합 중복을 거절한다")
    void createAdminAttributeCategoryRejectsDuplicate() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-duplicate-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        attributeCategoryRepository.save(new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));

        mockMvc.perform(post("/api/v1/admin/attribute-categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryType": "FLAVOR",
                                  "code": "SPICY",
                                  "name": "새 매운맛",
                                  "sortOrder": 20
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MENU_ATTRIBUTE_CATEGORY_DUPLICATE"));
    }

    @Test
    @DisplayName("관리자 attribute category 생성은 잘못된 categoryType을 공통 바디 검증 오류로 거절한다")
    void createAdminAttributeCategoryRejectsInvalidCategoryType() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-invalid-type-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));

        mockMvc.perform(post("/api/v1/admin/attribute-categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryType": "INVALID_TYPE",
                                  "code": "SPICY",
                                  "name": "매운맛",
                                  "sortOrder": 10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_BODY_FIELD"))
                .andExpect(jsonPath("$.error.details[0].field").value("categoryType"));
    }

    @Test
    @DisplayName("관리자는 attribute category의 수정 가능 필드를 갱신할 수 있다")
    void updateAdminAttributeCategory() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-update-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        AttributeCategory attributeCategory = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10)
        );

        mockMvc.perform(patch("/api/v1/admin/attribute-categories/{attributeCategoryId}", attributeCategory.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "순한맛",
                                  "sortOrder": 20,
                                  "isActive": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.id").value(attributeCategory.getId()))
                .andExpect(jsonPath("$.data.code").value("SPICY"))
                .andExpect(jsonPath("$.data.name").value("순한맛"))
                .andExpect(jsonPath("$.data.sortOrder").value(20))
                .andExpect(jsonPath("$.data.isActive").value(false));

        AttributeCategory updated = attributeCategoryRepository.findById(attributeCategory.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("순한맛");
        assertThat(updated.getSortOrder()).isEqualTo(20);
        assertThat(updated.isActive()).isFalse();
        assertThat(updated.getCode()).isEqualTo("SPICY");
        assertThat(updated.getCategoryType()).isEqualTo(CategoryType.FLAVOR);
    }

    @Test
    @DisplayName("관리자 attribute category 수정은 비활성 데이터를 다시 활성화할 수 있다")
    void updateAdminAttributeCategoryCanReactivate() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-reactivate-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        AttributeCategory attributeCategory = new AttributeCategory(CategoryType.FLAVOR, "MILD", "순한맛", 10);
        attributeCategory.deactivate();
        attributeCategory = attributeCategoryRepository.save(attributeCategory);

        mockMvc.perform(patch("/api/v1/admin/attribute-categories/{attributeCategoryId}", attributeCategory.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isActive": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(attributeCategory.getId()))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.name").value("순한맛"))
                .andExpect(jsonPath("$.data.sortOrder").value(10));
    }

    @Test
    @DisplayName("관리자 attribute category 수정은 존재하지 않는 대상을 거절한다")
    void updateAdminAttributeCategoryRejectsNotFound() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-not-found-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));

        mockMvc.perform(patch("/api/v1/admin/attribute-categories/{attributeCategoryId}", 999L)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새 이름"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MENU_ATTRIBUTE_CATEGORY_NOT_FOUND"));
    }

    @Test
    @DisplayName("일반 회원은 관리자 attribute category 수정에 접근할 수 없다")
    void updateAdminAttributeCategoryRejectsNonAdminMember() throws Exception {
        Member member = memberRepository.save(new Member(
                "member-patch-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        AttributeCategory attributeCategory = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10)
        );

        mockMvc.perform(patch("/api/v1/admin/attribute-categories/{attributeCategoryId}", attributeCategory.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "순한맛"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));
    }

    @Test
    @DisplayName("관리자는 attribute category를 비활성화할 수 있다")
    void deactivateAdminAttributeCategory() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-delete-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        AttributeCategory attributeCategory = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10)
        );

        mockMvc.perform(delete("/api/v1/admin/attribute-categories/{attributeCategoryId}", attributeCategory.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.id").value(attributeCategory.getId()))
                .andExpect(jsonPath("$.data.code").value("SPICY"))
                .andExpect(jsonPath("$.data.isActive").value(false));

        AttributeCategory deactivated = attributeCategoryRepository.findById(attributeCategory.getId()).orElseThrow();
        assertThat(deactivated.isActive()).isFalse();
        assertThat(deactivated.getName()).isEqualTo("매운맛");
        assertThat(deactivated.getCategoryType()).isEqualTo(CategoryType.FLAVOR);
    }

    @Test
    @DisplayName("관리자 attribute category 비활성화는 이미 비활성 상태여도 현재 상태를 반환한다")
    void deactivateAdminAttributeCategoryReturnsCurrentStateWhenAlreadyInactive() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-delete-inactive-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));
        AttributeCategory attributeCategory = new AttributeCategory(CategoryType.FLAVOR, "MILD", "순한맛", 10);
        attributeCategory.deactivate();
        attributeCategory = attributeCategoryRepository.save(attributeCategory);

        mockMvc.perform(delete("/api/v1/admin/attribute-categories/{attributeCategoryId}", attributeCategory.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(attributeCategory.getId()))
                .andExpect(jsonPath("$.data.isActive").value(false))
                .andExpect(jsonPath("$.data.name").value("순한맛"));
    }

    @Test
    @DisplayName("관리자 attribute category 비활성화는 존재하지 않는 대상을 거절한다")
    void deactivateAdminAttributeCategoryRejectsNotFound() throws Exception {
        Member admin = memberRepository.save(new Member(
                "admin-delete-not-found-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));

        mockMvc.perform(delete("/api/v1/admin/attribute-categories/{attributeCategoryId}", 999L)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(admin)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MENU_ATTRIBUTE_CATEGORY_NOT_FOUND"));
    }

    @Test
    @DisplayName("일반 회원은 관리자 attribute category 비활성화에 접근할 수 없다")
    void deactivateAdminAttributeCategoryRejectsNonAdminMember() throws Exception {
        Member member = memberRepository.save(new Member(
                "member-delete-user",
                "hashed-password",
                null,
                false,
                null,
                null,
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));
        AttributeCategory attributeCategory = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10)
        );

        mockMvc.perform(delete("/api/v1/admin/attribute-categories/{attributeCategoryId}", attributeCategory.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken(member)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private String accessToken(Member member) {
        SecretKey signingKey = Keys.hmacShaKeyFor(
                matchuriProperties.getAuth().getJwt().getSecret().getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
                .issuer(matchuriProperties.getAuth().getJwt().getIssuer())
                .subject(String.valueOf(member.getId()))
                .claim("role", member.getMemberRole().name())
                .claim("loginId", member.getLoginId())
                .claim("requiredAgreementRevision", RequiredAgreementVersions.currentRevision())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(1800)))
                .signWith(signingKey)
                .compact();
    }
}
