package matchuri.backend.domain.menu.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.menu.entity.Ingredient;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface IngredientRepository extends JpaRepository<Ingredient, Long>, IngredientRepositoryCustom {

    boolean existsByCode(String code);

    Optional<Ingredient> findByCode(String code);

    List<Ingredient> findAllByOrderBySortOrderAscIdAsc();

    List<Ingredient> findAllByIdInAndActiveTrue(Collection<Long> ids);
}
