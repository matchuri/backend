package matchuri.backend.domain.menu.repository.impl;

import static matchuri.backend.domain.image.entity.QImageAsset.imageAsset;
import static matchuri.backend.domain.menu.entity.QAttributeCategory.attributeCategory;
import static matchuri.backend.domain.menu.entity.QIngredient.ingredient;
import static matchuri.backend.domain.menu.entity.QMenuAttributeCategory.menuAttributeCategory;
import static matchuri.backend.domain.menu.entity.QMenuIngredient.menuIngredient;
import static matchuri.backend.domain.menu.entity.QMenuItem.menuItem;
import static matchuri.backend.domain.menu.entity.QMenuItemImage.menuItemImage;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.menu.entity.MenuItem;
import matchuri.backend.domain.menu.repository.MenuItemDetailRow;
import matchuri.backend.domain.menu.repository.MenuItemRepositoryCustom;
import matchuri.backend.domain.menu.repository.MenuRecommendationRow;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MenuItemRepositoryImpl implements MenuItemRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<MenuItemDetailRow> findActiveDetailRowById(Long menuItemId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(Projections.constructor(
                                MenuItemDetailRow.class,
                                menuItem.id,
                                menuItem.code,
                                menuItem.name,
                                menuItem.description,
                                imageAsset.objectKey
                        ))
                        .from(menuItem)
                        .leftJoin(menuItemImage).on(menuItemImage.menu.eq(menuItem))
                        .leftJoin(menuItemImage.imageAsset, imageAsset)
                        .where(
                                menuItem.id.eq(menuItemId),
                                menuItem.active.isTrue()
                        )
                        .fetchOne()
        );
    }

    @Override
    public List<MenuRecommendationRow> findActiveRecommendationRows() {
        return jpaQueryFactory
                .select(Projections.constructor(
                        MenuRecommendationRow.class,
                        menuItem.id,
                        menuItem.code,
                        menuItem.name
                ))
                .from(menuItem)
                .where(menuItem.active.isTrue())
                .orderBy(menuItem.id.asc())
                .fetch();
    }

    @Override
    public List<MenuItem> searchActiveMenuItems(@Nullable String query, Collection<Long> attributeCategoryIds, boolean attributeCategoryIdsEmpty, Collection<Long> ingredientIds, boolean ingredientIdsEmpty) {
        return jpaQueryFactory
                .selectFrom(menuItem)
                .where(
                        menuItem.active.isTrue(),
                        menuNameContains(query),
                        hasAnyAttributeCategory(attributeCategoryIds, attributeCategoryIdsEmpty),
                        hasAnyIngredient(ingredientIds, ingredientIdsEmpty)
                )
                .orderBy(menuItem.id.asc())
                .fetch();
    }

    private BooleanExpression menuNameContains(@Nullable String query) {
        return query == null ? null : menuItem.name.containsIgnoreCase(query);
    }

    private BooleanExpression hasAnyAttributeCategory(Collection<Long> attributeCategoryIds, boolean attributeCategoryIdsEmpty) {
        if (attributeCategoryIdsEmpty) {
            return null;
        }
        return JPAExpressions
                .selectOne()
                .from(menuAttributeCategory)
                .join(menuAttributeCategory.attributeCategory, attributeCategory)
                .where(
                        menuAttributeCategory.menu.eq(menuItem),
                        attributeCategory.active.isTrue(),
                        attributeCategory.id.in(attributeCategoryIds)
                )
                .exists();
    }

    private BooleanExpression hasAnyIngredient(Collection<Long> ingredientIds, boolean ingredientIdsEmpty) {
        if (ingredientIdsEmpty) {
            return null;
        }
        return JPAExpressions
                .selectOne()
                .from(menuIngredient)
                .join(menuIngredient.ingredient, ingredient)
                .where(
                        menuIngredient.menu.eq(menuItem),
                        ingredient.active.isTrue(),
                        ingredient.id.in(ingredientIds)
                )
                .exists();
    }
}
