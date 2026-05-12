package matchuri.backend.api.recommendation;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
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
class GuestRecommendationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        menuIngredientRepository.deleteAll();
        menuAttributeCategoryRepository.deleteAll();
        menuItemRepository.deleteAll();
        attributeCategoryRepository.deleteAll();
        ingredientRepository.deleteAll();
    }

    @Test
    @DisplayName("비회원 추천 API는 인증 없이 취향 입력 기반 후보를 반환한다")
    void createGuestPersonalRecommendationReturnsCandidatesWithoutAuthentication() throws Exception {
        AttributeCategory spicy = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        Ingredient peanut = ingredientRepository.save(new Ingredient("PEANUT", "땅콩", true, 10));

        MenuItem bibimbap = menuItemRepository.save(new MenuItem("BIBIMBAP", "비빔밥", "채소와 밥"));
        MenuItem peanutNoodle = menuItemRepository.save(new MenuItem("PEANUT_NOODLE", "땅콩면", "땅콩 소스 면"));
        MenuItem porkCutlet = menuItemRepository.save(new MenuItem("PORK_CUTLET", "돈까스", "튀김 메뉴"));
        MenuItem riceNoodle = menuItemRepository.save(new MenuItem("RICE_NOODLE", "쌀국수", "가벼운 국물 메뉴"));

        menuAttributeCategoryRepository.save(new MenuAttributeCategory(bibimbap, spicy));
        menuAttributeCategoryRepository.save(new MenuAttributeCategory(peanutNoodle, spicy));
        menuAttributeCategoryRepository.save(new MenuAttributeCategory(porkCutlet, spicy));
        menuIngredientRepository.save(new MenuIngredient(peanutNoodle, peanut));

        Map<String, Object> request = Map.of(
                "attributeCategoryIds", List.of(spicy.getId()),
                "restrictionIngredientIds", List.of(peanut.getId()),
                "dislikedMenuItemIds", List.of(porkCutlet.getId()),
                "contextJson", Map.of("mealTime", "LUNCH")
        );

        mockMvc.perform(post("/api/v1/guest/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.candidates.length()").value(2))
                .andExpect(jsonPath("$.data.candidates[0].menuId").value(bibimbap.getId()))
                .andExpect(jsonPath("$.data.candidates[0].menuName").value("비빔밥"))
                .andExpect(jsonPath("$.data.candidates[0].rankNo").value(1))
                .andExpect(jsonPath("$.data.candidates[0].id").doesNotExist())
                .andExpect(jsonPath("$.data.requestId").doesNotExist())
                .andExpect(jsonPath("$.data.candidates[1].menuId").value(riceNoodle.getId()));
    }

    @Test
    @DisplayName("비회원 추천 API는 중복된 attribute category ID를 거부한다")
    void createGuestPersonalRecommendationRejectsDuplicateAttributeCategoryIds() throws Exception {
        Map<String, Object> request = Map.of(
                "attributeCategoryIds", List.of(1L, 1L),
                "restrictionIngredientIds", List.of(),
                "dislikedMenuItemIds", List.of(),
                "contextJson", Map.of()
        );

        mockMvc.perform(post("/api/v1/guest/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("GUEST_RECOMMENDATION_DUPLICATE_ATTRIBUTE_CATEGORY"));
    }
}
