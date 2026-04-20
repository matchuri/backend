package matchuri.backend.domain.menu.repository;

import java.util.Collection;
import java.util.List;
import matchuri.backend.domain.menu.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    boolean existsByCode(String code);

    List<Ingredient> findAllByActiveTrueOrderBySortOrderAscIdAsc();

    List<Ingredient> findAllByIdInAndActiveTrue(Collection<Long> ids);
}
