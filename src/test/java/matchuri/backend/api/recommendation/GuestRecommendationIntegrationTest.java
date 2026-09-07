package matchuri.backend.api.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import matchuri.backend.domain.image.entity.ImageAsset;
import matchuri.backend.domain.image.entity.ImageStorageProvider;
import matchuri.backend.domain.image.repository.ImageAssetRepository;
import matchuri.backend.domain.menu.entity.AttributeCategory;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.entity.MenuAttributeCategory;
import matchuri.backend.domain.menu.entity.MenuIngredient;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.entity.MenuItemImage;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.repository.MenuAttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.MenuIngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import matchuri.backend.domain.menu.repository.MenuItemImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
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
    private MenuItemImageRepository menuItemImageRepository;

    @Autowired
    private ImageAssetRepository imageAssetRepository;

    @Autowired
    private MenuAttributeCategoryRepository menuAttributeCategoryRepository;

    @Autowired
    private MenuIngredientRepository menuIngredientRepository;

    @BeforeEach
    void setUp() {
        menuIngredientRepository.deleteAll();
        menuAttributeCategoryRepository.deleteAll();
        menuItemImageRepository.deleteAll();
        menuItemRepository.deleteAll();
        imageAssetRepository.deleteAll();
        attributeCategoryRepository.deleteAll();
        ingredientRepository.deleteAll();
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("비회원 추천은 메뉴 수와 무관하게 고정 쿼리로 응답한다")
    void measureGuestRecommendationQueryScaleAfterOptimization(CapturedOutput output) throws Exception {
        AttributeCategory category = attributeCategoryRepository.save(
                new AttributeCategory(CategoryType.FLAVOR, "BASELINE", "계측", 10));
        Map<String, Object> request = Map.of(
                "attributeCategoryIds", List.of(category.getId()),
                "restrictionIngredientIds", List.of(),
                "dislikedMenuItemIds", List.of(),
                "contextJson", Map.of()
        );

        MenuItem firstMenu = saveMeasuredMenus(category, 1, 1).getFirst();
        ImageAsset imageAsset = imageAssetRepository.save(new ImageAsset(
                ImageStorageProvider.CLOUDFLARE_R2,
                "test",
                "guest-recommendation/baseline.png",
                "baseline.png",
                "image/png",
                1024L,
                "guest-recommendation-baseline-checksum",
                640,
                480
        ));
        menuItemImageRepository.save(new MenuItemImage(firstMenu, imageAsset));
        mockMvc.perform(post("/api/v1/guest/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidates.length()").value(1))
                .andExpect(jsonPath("$.data.candidates[0].thumbnailUrl")
                        .value("https://asset.matchuri.com/guest-recommendation/baseline.png"));

        saveMeasuredMenus(category, 2, 12);
        mockMvc.perform(post("/api/v1/guest/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidates.length()").value(3));

        List<String> queryLogs = output.getOut().lines()
                .filter(line -> line.contains(
                        "API_QUERY_BEFORE method=POST uri=/api/v1/guest/recommendations status=200"))
                .toList();

        assertThat(queryLogs)
                .hasSize(2)
                .allMatch(line -> line.contains(
                        "total=7 select=7 insert=0 update=0 delete=0 other=0"));
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

    private List<MenuItem> saveMeasuredMenus(AttributeCategory category, int startInclusive, int endInclusive) {
        List<MenuItem> menuItems = new ArrayList<>();
        for (int number = startInclusive; number <= endInclusive; number++) {
            MenuItem menuItem = menuItemRepository.save(
                    new MenuItem("BASELINE_" + number, "계측 메뉴 " + number, "계측 설명"));
            menuAttributeCategoryRepository.save(new MenuAttributeCategory(menuItem, category));
            menuItems.add(menuItem);
        }
        return menuItems;
    }
}
