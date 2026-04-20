package matchuri.backend.api.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
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

    @BeforeEach
    void setUp() {
        attributeCategoryRepository.deleteAll();
        ingredientRepository.deleteAll();
    }

    @Test
    @DisplayName("attribute category 목록 조회는 활성 데이터만 정렬해서 반환한다")
    void getAttributeCategoriesReturnsOnlyActiveRowsInSortedOrder() throws Exception {
        AttributeCategory spicy = attributeCategoryRepository.save(new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 20));
        AttributeCategory grilled = attributeCategoryRepository.save(new AttributeCategory(CategoryType.COOKING_METHOD, "GRILLED", "구이", 10));
        AttributeCategory sweet = attributeCategoryRepository.save(new AttributeCategory(CategoryType.FLAVOR, "SWEET", "달콤함", 10));
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
}
