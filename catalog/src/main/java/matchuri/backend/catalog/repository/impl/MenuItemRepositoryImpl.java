package matchuri.backend.catalog.repository.impl;

import static matchuri.backend.media.entity.QImageAsset.imageAsset;
import static matchuri.backend.catalog.entity.QAttributeCategory.attributeCategory;
import static matchuri.backend.catalog.entity.QIngredient.ingredient;
import static matchuri.backend.catalog.entity.QMenuAttributeCategory.menuAttributeCategory;
import static matchuri.backend.catalog.entity.QMenuIngredient.menuIngredient;
import static matchuri.backend.catalog.entity.QMenuItem.menuItem;
import static matchuri.backend.catalog.entity.QMenuItemImage.menuItemImage;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import matchuri.backend.catalog.entity.MenuItem;
import matchuri.backend.catalog.repository.MenuItemDetailRow;
import matchuri.backend.catalog.repository.MenuItemRepositoryCustom;
import matchuri.backend.catalog.api.query.MenuRecommendationRow;
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
