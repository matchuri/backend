package matchuri.backend.api.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import matchuri.backend.domain.menu.entity.MenuAttributeCategory;
import matchuri.backend.domain.menu.entity.MenuIngredient;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.repository.MenuAttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.MenuIngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MenuReferenceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

    @BeforeEach
    void setUp() {
        menuAttributeCategoryRepository.deleteAll();
        menuIngredientRepository.deleteAll();
        menuItemRepository.deleteAll();
        attributeCategoryRepository.deleteAll();
        ingredientRepository.deleteAll();
    }

    @Test
    @DisplayName("attribute category 목록 조회는 활성 데이터만 정렬해서 반환한다")
    void getAttributeCategoriesReturnsOnlyActiveRowsInSortedOrder() throws Exception {
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 20));
        AttributeCategory grilled = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.COOKING_METHOD, "GRILLED", "구이", 10));
        AttributeCategory sweet = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SWEET", "달콤함", 10));
        AttributeCategory inactive = new AttributeCategory(CategoryType.FLAVOR, "MILD", "순한맛", 5);
        inactive.deactivate();
        attributeCategoryRepository.save(inactive);

        mockMvc.perform(get("/api/v1/attribute-categories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].id").value(grilled.getId()))
                .andExpect(jsonPath("$.data[0].categoryType").value("COOKING_METHOD"))
                .andExpect(jsonPath("$.data[0].code").value("GRILLED"))
                .andExpect(jsonPath("$.data[1].id").value(sweet.getId()))
                .andExpect(jsonPath("$.data[1].categoryType").value("FLAVOR"))
                .andExpect(jsonPath("$.data[1].code").value("SWEET"))
                .andExpect(jsonPath("$.data[2].id").value(spicy.getId()))
                .andExpect(jsonPath("$.data[2].code").value("SPICY"));

        assertThat(attributeCategoryRepository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("restriction ingredient 목록 조회는 공개 API로 활성 데이터만 정렬해서 반환한다")
    void getRestrictionIngredientsReturnsOnlyActiveRowsInSortedOrder() throws Exception {
        Ingredient peanut = ingredientRepository.save(new Ingredient("PEANUT", "땅콩", true, 10));
        Ingredient shrimp = ingredientRepository.save(new Ingredient("SHRIMP", "새우", true, 10));
        ingredientRepository.save(new Ingredient("MILK", "우유", true, 30));
        Ingredient inactive = new Ingredient("PORK", "돼지고기", false, 5);
        inactive.deactivate();
        ingredientRepository.save(inactive);

        mockMvc.perform(get("/api/v1/restriction-ingredients").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].id").value(peanut.getId()))
                .andExpect(jsonPath("$.data[0].code").value("PEANUT"))
                .andExpect(jsonPath("$.data[0].allergen").value(true))
                .andExpect(jsonPath("$.data[1].id").value(shrimp.getId()))
                .andExpect(jsonPath("$.data[1].code").value("SHRIMP"))
                .andExpect(jsonPath("$.data[2].code").value("MILK"));
    }

    @Test
    @DisplayName("메뉴 목록 조회는 활성 메뉴의 id, code, name만 반환한다")
    void searchMenuItemsReturnsOnlyActiveMenuSummaries() throws Exception {
        MenuItem kimchiStew = menuItemRepository.save(
                new MenuItem("KIMCHI_STEW", "김치찌개", "김치와 돼지고기를 넣고 끓인 찌개"));
        MenuItem porkCutlet = menuItemRepository.save(
                new MenuItem("PORK_CUTLET", "돈까스", "바삭한 돼지고기 튀김"));
        MenuItem inactive = new MenuItem("SUSHI", "초밥", "밥 위에 생선을 올린 메뉴");
        inactive.deactivate();
        menuItemRepository.save(inactive);

        mockMvc.perform(get("/api/v1/menu-items").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(kimchiStew.getId()))
                .andExpect(jsonPath("$.data[0].code").value("KIMCHI_STEW"))
                .andExpect(jsonPath("$.data[0].name").value("김치찌개"))
                .andExpect(jsonPath("$.data[0].description").doesNotExist())
                .andExpect(jsonPath("$.data[1].id").value(porkCutlet.getId()))
                .andExpect(jsonPath("$.data[1].code").value("PORK_CUTLET"))
                .andExpect(jsonPath("$.data[1].name").value("돈까스"));
    }

    @Test
    @DisplayName("메뉴 목록 조회는 메뉴명 부분 검색을 지원한다")
    void searchMenuItemsFiltersByNameQuery() throws Exception {
        menuItemRepository.save(new MenuItem("KIMCHI_STEW", "김치찌개", "김치와 돼지고기를 넣고 끓인 찌개"));
        menuItemRepository.save(new MenuItem("DOENJANG_STEW", "된장찌개", "된장을 넣고 끓인 찌개"));
        menuItemRepository.save(new MenuItem("PORK_CUTLET", "돈까스", "바삭한 돼지고기 튀김"));

        mockMvc.perform(get("/api/v1/menu-items")
                        .param("query", "찌개")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].code").value("KIMCHI_STEW"))
                .andExpect(jsonPath("$.data[1].code").value("DOENJANG_STEW"));
    }

    @Test
    @DisplayName("메뉴 목록 조회 필터는 그룹 내부 OR, 그룹 간 AND로 동작한다")
    void searchMenuItemsCombinesFiltersWithOrInsideGroupsAndAndBetweenGroups() throws Exception {
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        AttributeCategory crispy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.TEXTURE, "CRISPY", "바삭함", 20));
        AttributeCategory soup = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.COOKING_METHOD, "SOUP", "국물", 30));
        Ingredient pork = ingredientRepository.save(new Ingredient("PORK", "돼지고기", false, 10));
        Ingredient shrimp = ingredientRepository.save(new Ingredient("SHRIMP", "새우", true, 20));

        MenuItem kimchiStew = menuItemRepository.save(
                new MenuItem("KIMCHI_STEW", "김치찌개", "김치와 돼지고기를 넣고 끓인 찌개"));
        MenuItem jjampong = menuItemRepository.save(
                new MenuItem("JJAMPPONG", "짬뽕", "해산물과 채소를 넣은 매콤한 국물 면 요리"));
        MenuItem porkCutlet = menuItemRepository.save(
                new MenuItem("PORK_CUTLET", "돈까스", "바삭한 돼지고기 튀김"));

        mapAttributeCategory(kimchiStew, spicy);
        mapAttributeCategory(kimchiStew, soup);
        mapIngredient(kimchiStew, pork);
        mapAttributeCategory(jjampong, spicy);
        mapAttributeCategory(jjampong, soup);
        mapIngredient(jjampong, shrimp);
        mapAttributeCategory(porkCutlet, crispy);
        mapIngredient(porkCutlet, pork);

        mockMvc.perform(get("/api/v1/menu-items")
                        .param("attributeCategoryIds", spicy.getId().toString(), crispy.getId().toString())
                        .param("ingredientIds", pork.getId().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].code").value("KIMCHI_STEW"))
                .andExpect(jsonPath("$.data[1].code").value("PORK_CUTLET"));
    }

    @Test
    @DisplayName("메뉴 목록 조회는 잘못된 attribute category 필터 ID를 거절한다")
    void searchMenuItemsRejectsInvalidAttributeCategoryFilterIds() throws Exception {
        mockMvc.perform(get("/api/v1/menu-items")
                        .param("attributeCategoryIds", "999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MENU_INVALID_FILTER"));
    }

    @Test
    @DisplayName("메뉴 목록 조회는 잘못된 ingredient 필터 ID를 거절한다")
    void searchMenuItemsRejectsInvalidIngredientFilterIds() throws Exception {
        mockMvc.perform(get("/api/v1/menu-items")
                        .param("ingredientIds", "999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MENU_INVALID_FILTER"));
    }

    private void mapAttributeCategory(MenuItem menuItem, AttributeCategory attributeCategory) {
        menuAttributeCategoryRepository.save(new MenuAttributeCategory(menuItem, attributeCategory));
    }

    private void mapIngredient(MenuItem menuItem, Ingredient ingredient) {
        menuIngredientRepository.save(new MenuIngredient(menuItem, ingredient));
    }
}
