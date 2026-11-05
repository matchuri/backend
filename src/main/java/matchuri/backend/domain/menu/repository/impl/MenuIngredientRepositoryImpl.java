package matchuri.backend.domain.menu.repository.impl;

import static matchuri.backend.domain.menu.entity.QIngredient.ingredient;
import static matchuri.backend.domain.menu.entity.QMenuIngredient.menuIngredient;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.menu.repository.MenuIngredientIdRow;
import matchuri.backend.domain.menu.repository.MenuIngredientRepositoryCustom;
import matchuri.backend.domain.menu.repository.MenuRestrictionIngredientRow;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MenuIngredientRepositoryImpl implements MenuIngredientRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MenuRestrictionIngredientRow> findActiveRowsByMenuId(Long menuId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        MenuRestrictionIngredientRow.class,
                        ingredient.id,
                        ingredient.code,
                        ingredient.name,
                        ingredient.allergen,
                        ingredient.sortOrder
                ))
                .from(menuIngredient)
                .join(menuIngredient.ingredient, ingredient)
                .where(
                        menuIngredient.menu.id.eq(menuId),
                        ingredient.active.isTrue()
                )
                .orderBy(
                        ingredient.sortOrder.asc(),
                        ingredient.id.asc()
                )
                .fetch();
    }

    @Override
    public List<MenuIngredientIdRow> findIdRowsByMenuIds(Collection<Long> menuIds) {
        if (menuIds.isEmpty()) {
            return List.of();
        }

        return jpaQueryFactory
                .select(Projections.constructor(
                        MenuIngredientIdRow.class,
                        menuIngredient.menu.id,
                        ingredient.id
                ))
                .from(menuIngredient)
                .join(menuIngredient.ingredient, ingredient)
                .where(menuIngredient.menu.id.in(menuIds))
                .orderBy(menuIngredient.id.asc())
                .fetch();
    }
}
