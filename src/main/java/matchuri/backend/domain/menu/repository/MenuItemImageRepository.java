package matchuri.backend.domain.menu.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import matchuri.backend.domain.menu.entity.MenuItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemImageRepository extends JpaRepository<MenuItemImage, Long> {

    Optional<MenuItemImage> findByMenuId(Long menuId);

    List<MenuItemImage> findAllByMenuIdIn(Collection<Long> menuIds);
}
