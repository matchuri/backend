package matchuri.backend.domain.menu.repository;

import java.util.List;
import matchuri.backend.domain.menu.entity.MenuIngredient;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

@NullMarked
public interface MenuIngredientRepository extends JpaRepository<MenuIngredient, Long> {

    List<MenuIngredient> findAllByMenuIdAndIngredientActiveTrueOrderByIngredientSortOrderAscIngredientIdAsc(Long menuId);
}
