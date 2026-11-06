package matchuri.backend.domain.menu;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import matchuri.backend.global.config.JpaConfig;
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
import matchuri.backend.domain.menu.repository.MenuRecommendationRow;
import matchuri.backend.domain.menu.repository.MenuAttributeCategoryIdRow;
import matchuri.backend.domain.menu.repository.MenuIngredientIdRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest(showSql = false)
@Import(JpaConfig.class)
@ActiveProfiles("test")
class MenuRecommendationRowsRepositoryTest {

    @Autowired private MenuItemRepository menus;
    @Autowired private AttributeCategoryRepository categories;
    @Autowired private IngredientRepository ingredients;
    @Autowired private MenuAttributeCategoryRepository menuCategories;
    @Autowired private MenuIngredientRepository menuIngredients;

    @Test
    @DisplayName("추천 메뉴 조회는 활성 메뉴만 반환하고 연결된 속성·재료의 활성 여부와 무관하게 ID를 보존한다")
    void returnsActiveMenusWithAllLinkedIdsAndEmptyMissingAssociations() {
        MenuItem inactive = new MenuItem("INACTIVE", "중단 메뉴", "");
        inactive.deactivate();
        menus.save(inactive);
        MenuItem linked = menus.save(new MenuItem("LINKED", "속성·재료 있는 메뉴", ""));
        MenuItem plain = menus.save(new MenuItem("PLAIN", "속성·재료 없는 메뉴", ""));

        AttributeCategory activeCategory = categories.save(
                new AttributeCategory(CategoryType.FLAVOR, "SPICY", "매운맛", 10));
        AttributeCategory inactiveCategory = new AttributeCategory(CategoryType.FLAVOR, "OLD", "기존 속성", 20);
        inactiveCategory.deactivate();
        categories.save(inactiveCategory);
        Ingredient activeIngredient = ingredients.save(new Ingredient("RICE", "쌀", false, 10));
        Ingredient inactiveIngredient = new Ingredient("OLD_INGREDIENT", "기존 재료", true, 20);
        inactiveIngredient.deactivate();
        ingredients.save(inactiveIngredient);

        menuCategories.saveAll(List.of(
                new MenuAttributeCategory(linked, activeCategory),
                new MenuAttributeCategory(linked, inactiveCategory),
                new MenuAttributeCategory(inactive, activeCategory)
        ));
        menuIngredients.saveAll(List.of(
                new MenuIngredient(linked, activeIngredient),
                new MenuIngredient(linked, inactiveIngredient),
                new MenuIngredient(inactive, activeIngredient)
        ));

        assertThat(menus.findActiveRecommendationRows()).containsExactly(
                new MenuRecommendationRow(linked.getId(), "LINKED", "속성·재료 있는 메뉴"),
                new MenuRecommendationRow(plain.getId(), "PLAIN", "속성·재료 없는 메뉴")
        );
        List<Long> menuIds = List.of(linked.getId(), plain.getId());
        assertThat(menuCategories.findIdRowsByMenuIds(menuIds)).containsExactly(
                new MenuAttributeCategoryIdRow(linked.getId(), activeCategory.getId()),
                new MenuAttributeCategoryIdRow(linked.getId(), inactiveCategory.getId())
        );
        assertThat(menuIngredients.findIdRowsByMenuIds(menuIds)).containsExactly(
                new MenuIngredientIdRow(linked.getId(), activeIngredient.getId()),
                new MenuIngredientIdRow(linked.getId(), inactiveIngredient.getId())
        );
        assertThat(menuCategories.findIdRowsByMenuIds(List.of(plain.getId()))).isEmpty();
        assertThat(menuIngredients.findIdRowsByMenuIds(List.of(plain.getId()))).isEmpty();
    }

    @Test
    @DisplayName("추천 가능한 활성 메뉴가 없으면 빈 결과를 반환한다")
    void returnsEmptyWhenAllMenusAreInactive() {
        MenuItem inactive = new MenuItem("INACTIVE_ONLY", "중단 메뉴", "");
        inactive.deactivate();
        menus.save(inactive);

        assertThat(menus.findActiveRecommendationRows()).isEmpty();
    }
}
