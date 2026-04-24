package matchuri.backend.domain.menu.repository;

import matchuri.backend.domain.menu.entity.MenuIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuIngredientRepository extends JpaRepository<MenuIngredient, Long> {
}
