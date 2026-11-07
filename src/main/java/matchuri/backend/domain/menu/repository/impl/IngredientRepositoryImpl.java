package matchuri.backend.domain.menu.repository.impl;

import static matchuri.backend.domain.menu.entity.QIngredient.ingredient;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.menu.entity.Ingredient;
import matchuri.backend.domain.menu.repository.IngredientRepositoryCustom;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IngredientRepositoryImpl implements IngredientRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Ingredient> searchActiveRestrictionIngredients(@Nullable String query, @Nullable Boolean allergen) {
        return jpaQueryFactory
                .selectFrom(ingredient)
                .where(
                        ingredient.active.isTrue(),
                        nameContains(query),
                        allergenEquals(allergen)
                )
                .orderBy(ingredient.sortOrder.asc(), ingredient.id.asc())
                .fetch();
    }

    private BooleanExpression nameContains(@Nullable String query) {
        return query == null ? null : ingredient.name.containsIgnoreCase(query);
    }

    private BooleanExpression allergenEquals(@Nullable Boolean allergen) {
        return allergen == null ? null : ingredient.allergen.eq(allergen);
    }
}
