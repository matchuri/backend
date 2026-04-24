package matchuri.backend.domain.menu.repository;

import java.util.Collection;
import java.util.List;
import matchuri.backend.domain.menu.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    boolean existsByCode(String code);

    List<Ingredient> findAllByOrderBySortOrderAscIdAsc();

    List<Ingredient> findAllByActiveTrueOrderBySortOrderAscIdAsc();

    @Query("""
            select ingredient
            from Ingredient ingredient
            where ingredient.active = true
              and (:query is null or lower(ingredient.name) like lower(concat('%', :query, '%')))
              and (:allergen is null or ingredient.allergen = :allergen)
            order by ingredient.sortOrder asc, ingredient.id asc
            """)
    List<Ingredient> searchActiveRestrictionIngredients(
            @Param("query") String query,
            @Param("allergen") Boolean allergen
    );

    List<Ingredient> findAllByIdInAndActiveTrue(Collection<Long> ids);
}
