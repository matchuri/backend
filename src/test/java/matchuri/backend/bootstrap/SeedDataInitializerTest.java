package matchuri.backend.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import matchuri.backend.domain.menu.repository.MenuAttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.MenuIngredientRepository;
import matchuri.backend.domain.menu.repository.MenuItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "matchuri.seed.enabled=true",
        "matchuri.seed.sample-members-enabled=true"
})
@ActiveProfiles({"test", "local"})
class SeedDataInitializerTest {

    @Autowired
    private SeedDataInitializer seedDataInitializer;

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

    @Test
    @DisplayName("local 프로필에서는 참조 데이터와 샘플 회원 시드가 멱등하게 초기화된다")
    void initializesReferenceAndSampleDataIdempotently() throws Exception {
        long initialCount = memberRepository.count();
        long initialAttributeCategoryCount = attributeCategoryRepository.count();
        long initialIngredientCount = ingredientRepository.count();
        long initialMenuItemCount = menuItemRepository.count();
        long initialMenuAttributeCategoryCount = menuAttributeCategoryRepository.count();
        long initialMenuIngredientCount = menuIngredientRepository.count();

        assertThat(memberRepository.existsByLoginId("tester01")).isTrue();
        assertThat(memberRepository.existsByLoginId("tester02")).isTrue();
        assertThat(attributeCategoryRepository.existsByCategoryTypeAndCode(CategoryType.FLAVOR, "SPICY")).isTrue();
        assertThat(attributeCategoryRepository.existsByCategoryTypeAndCode(CategoryType.FLAVOR, "RICH")).isTrue();
        assertThat(attributeCategoryRepository.existsByCategoryTypeAndCode(CategoryType.COOKING_METHOD, "STIR_FRIED"))
                .isTrue();
        assertThat(attributeCategoryRepository.existsByCategoryTypeAndCode(CategoryType.FOOD_CATEGORY, "ASIAN"))
                .isTrue();
        assertThat(attributeCategoryRepository.existsByCategoryTypeAndCode(CategoryType.TEXTURE, "CHEWY")).isTrue();
        assertThat(attributeCategoryRepository.existsByCategoryTypeAndCode(CategoryType.TEMPERATURE, "HOT")).isTrue();
        assertThat(attributeCategoryRepository.existsByCategoryTypeAndCode(CategoryType.TEMPERATURE, "COLD")).isTrue();
        assertThat(ingredientRepository.existsByCode("PEANUT")).isTrue();
        assertThat(ingredientRepository.existsByCode("EGG")).isTrue();
        assertThat(ingredientRepository.existsByCode("WHEAT")).isTrue();
        assertThat(ingredientRepository.existsByCode("CILANTRO")).isTrue();
        assertThat(ingredientRepository.existsByCode("PINE_NUT")).isTrue();
        assertThat(ingredientRepository.existsByCode("WALNUT")).isTrue();
        assertThat(ingredientRepository.existsByCode("TOMATO")).isTrue();
        assertThat(ingredientRepository.existsByCode("SULFITES")).isTrue();
        assertThat(ingredientRepository.existsByCode("CASHEW_NUT")).isTrue();
        assertThat(ingredientRepository.existsByCode("EGGPLANT")).isTrue();
        assertThat(ingredientRepository.existsByCode("CUCUMBER")).isTrue();
        assertThat(ingredientRepository.existsByCode("ONION")).isTrue();
        assertThat(ingredientRepository.existsByCode("PERILLA_LEAF")).isTrue();
        assertThat(ingredientRepository.existsByCode("AROMATIC_SPICES")).isTrue();
        assertThat(ingredientByCode("PORK").isAllergen()).isTrue();
        assertThat(ingredientByCode("BEEF").isAllergen()).isTrue();
        assertThat(ingredientByCode("BEEF").getName()).isEqualTo("쇠고기");
        assertThat(ingredientByCode("CHICKEN").isAllergen()).isTrue();
        assertThat(ingredientByCode("MUSHROOM").getName()).isEqualTo("버섯류");
        assertThat(menuItemRepository.existsByCode("KIMCHI_STEW")).isTrue();
        assertThat(menuItemRepository.existsByCode("PORK_CUTLET")).isTrue();
        assertThat(menuItemRepository.existsByCode("PAD_THAI")).isTrue();
        assertThat(menuItemRepository.existsByCode("SALAD")).isTrue();
        assertThat(initialMenuAttributeCategoryCount).isPositive();
        assertThat(initialMenuIngredientCount).isPositive();

        seedDataInitializer.run(new DefaultApplicationArguments(new String[0]));

        assertThat(memberRepository.count()).isEqualTo(initialCount);
        assertThat(attributeCategoryRepository.count()).isEqualTo(initialAttributeCategoryCount);
        assertThat(ingredientRepository.count()).isEqualTo(initialIngredientCount);
        assertThat(menuItemRepository.count()).isEqualTo(initialMenuItemCount);
        assertThat(menuAttributeCategoryRepository.count()).isEqualTo(initialMenuAttributeCategoryCount);
        assertThat(menuIngredientRepository.count()).isEqualTo(initialMenuIngredientCount);
        assertThat(memberRepository.existsByLoginId("tester01")).isTrue();
        assertThat(memberRepository.existsByLoginId("tester02")).isTrue();
        assertThat(attributeCategoryRepository.existsByCategoryTypeAndCode(CategoryType.FLAVOR, "SPICY")).isTrue();
        assertThat(ingredientRepository.existsByCode("PEANUT")).isTrue();
        assertThat(menuItemRepository.existsByCode("KIMCHI_STEW")).isTrue();
    }

    private Ingredient ingredientByCode(String code) {
        return ingredientRepository.findByCode(code)
                .orElseThrow(() -> new AssertionError("Expected ingredient seed. code=" + code));
    }
}
