package matchuri.backend.domain.menu.repository.impl;

import static matchuri.backend.domain.image.entity.QImageAsset.imageAsset;
import static matchuri.backend.domain.menu.entity.QAttributeCategory.attributeCategory;
import static matchuri.backend.domain.menu.entity.QIngredient.ingredient;
import static matchuri.backend.domain.menu.entity.QMenuAttributeCategory.menuAttributeCategory;
import static matchuri.backend.domain.menu.entity.QMenuIngredient.menuIngredient;
import static matchuri.backend.domain.menu.entity.QMenuItem.menuItem;
import static matchuri.backend.domain.menu.entity.QMenuItemImage.menuItemImage;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.menu.repository.MenuItemDetailQueryResult;
import matchuri.backend.domain.menu.repository.MenuItemDetailQueryResult.AttributeCategoryRow;
import matchuri.backend.domain.menu.repository.MenuItemDetailQueryResult.RestrictionIngredientRow;
import matchuri.backend.domain.menu.repository.MenuItemRepositoryCustom;
import matchuri.backend.domain.menu.repository.MenuRecommendationProfileQueryResult;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MenuItemRepositoryImpl implements MenuItemRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<MenuItemDetailQueryResult> findActiveMenuItemDetailById(Long menuItemId) {
        Tuple menuRow = jpaQueryFactory
                .select(
                        menuItem.id,
                        menuItem.code,
                        menuItem.name,
                        menuItem.description,
                        imageAsset.objectKey
                )
                .from(menuItem)
                .leftJoin(menuItemImage).on(menuItemImage.menu.eq(menuItem))
                .leftJoin(menuItemImage.imageAsset, imageAsset)
                .where(
                        menuItem.id.eq(menuItemId),
                        menuItem.active.isTrue()
                )
                .fetchOne();

        if (menuRow == null) {
            return Optional.empty();
        }

        List<AttributeCategoryRow> attributeCategories = jpaQueryFactory
                .select(Projections.constructor(
                        AttributeCategoryRow.class,
                        attributeCategory.id,
                        attributeCategory.categoryType,
                        attributeCategory.code,
                        attributeCategory.name,
                        attributeCategory.sortOrder
                ))
                .from(menuAttributeCategory)
                .join(menuAttributeCategory.attributeCategory, attributeCategory)
                .where(
                        menuAttributeCategory.menu.id.eq(menuItemId),
                        attributeCategory.active.isTrue()
                )
                .orderBy(
                        attributeCategory.categoryType.asc(),
                        attributeCategory.sortOrder.asc(),
                        attributeCategory.id.asc()
                )
                .fetch();

        List<RestrictionIngredientRow> ingredients = jpaQueryFactory
                .select(Projections.constructor(
                        RestrictionIngredientRow.class,
                        ingredient.id,
                        ingredient.code,
                        ingredient.name,
                        ingredient.allergen,
                        ingredient.sortOrder
                ))
                .from(menuIngredient)
                .join(menuIngredient.ingredient, ingredient)
                .where(
                        menuIngredient.menu.id.eq(menuItemId),
                        ingredient.active.isTrue()
                )
                .orderBy(
                        ingredient.sortOrder.asc(),
                        ingredient.id.asc()
                )
                .fetch();

        return Optional.of(new MenuItemDetailQueryResult(
                menuRow.get(menuItem.id),
                menuRow.get(menuItem.code),
                menuRow.get(menuItem.name),
                menuRow.get(menuItem.description),
                menuRow.get(imageAsset.objectKey),
                attributeCategories,
                ingredients
        ));
    }

    @Override
    public List<MenuRecommendationProfileQueryResult> findActiveMenuRecommendationProfiles() {
        List<Tuple> menuRows = jpaQueryFactory
                .select(menuItem.id, menuItem.code, menuItem.name)
                .from(menuItem)
                .where(menuItem.active.isTrue())
                .orderBy(menuItem.id.asc())
                .fetch();

        if (menuRows.isEmpty()) {
            return List.of();
        }

        List<Long> menuIds = menuRows.stream()
                .map(row -> row.get(menuItem.id))
                .toList();
        Map<Long, List<Long>> attributeCategoryIdsByMenuId = jpaQueryFactory
                .select(menuAttributeCategory.menu.id, attributeCategory.id)
                .from(menuAttributeCategory)
                .join(menuAttributeCategory.attributeCategory, attributeCategory)
                .where(menuAttributeCategory.menu.id.in(menuIds))
                .orderBy(menuAttributeCategory.id.asc())
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        row -> row.get(menuAttributeCategory.menu.id),
                        LinkedHashMap::new,
                        Collectors.mapping(row -> row.get(attributeCategory.id), Collectors.toList())
                ));
        Map<Long, List<Long>> ingredientIdsByMenuId = jpaQueryFactory
                .select(menuIngredient.menu.id, ingredient.id)
                .from(menuIngredient)
                .join(menuIngredient.ingredient, ingredient)
                .where(menuIngredient.menu.id.in(menuIds))
                .orderBy(menuIngredient.id.asc())
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        row -> row.get(menuIngredient.menu.id),
                        LinkedHashMap::new,
                        Collectors.mapping(row -> row.get(ingredient.id), Collectors.toList())
                ));

        return menuRows.stream()
                .map(row -> {
                    Long menuId = row.get(menuItem.id);
                    return new MenuRecommendationProfileQueryResult(
                            menuId,
                            row.get(menuItem.code),
                            row.get(menuItem.name),
                            attributeCategoryIdsByMenuId.getOrDefault(menuId, List.of()),
                            ingredientIdsByMenuId.getOrDefault(menuId, List.of())
                    );
                })
                .toList();
    }
}
