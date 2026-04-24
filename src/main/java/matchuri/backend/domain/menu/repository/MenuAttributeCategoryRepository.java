package matchuri.backend.domain.menu.repository;

import matchuri.backend.domain.menu.entity.MenuAttributeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuAttributeCategoryRepository extends JpaRepository<MenuAttributeCategory, Long> {
}
