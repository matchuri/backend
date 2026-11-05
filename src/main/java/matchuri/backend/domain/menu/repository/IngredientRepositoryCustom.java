package matchuri.backend.domain.menu.repository;

import java.util.List;
import matchuri.backend.domain.menu.entity.Ingredient;
import org.jspecify.annotations.Nullable;

public interface IngredientRepositoryCustom {

    List<Ingredient> searchActiveRestrictionIngredients(@Nullable String query, @Nullable Boolean allergen);
}
